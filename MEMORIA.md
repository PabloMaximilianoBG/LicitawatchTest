# MEMORIA.md — LicitaWatch

Registro cronológico de avance del proyecto. Si retomas este proyecto en otra
sesión, lee esto primero.

## Contexto general

- Proyecto de título de Ingeniería. Plataforma privada de gestión de
  licitaciones (no depende de Mercado Público ni de ningún portal externo).
- Arquitectura fuente de verdad: `LicitaWatch_OFICIAL.pptx` (revisada al
  inicio del proyecto). Coincide con el prompt maestro del usuario en
  prácticamente todo. Única discrepancia: las diapositivas 9 y 14 mencionan
  Docker para desarrollo local; el usuario override explícitamente esto —
  **todo corre nativo en Windows, sin Docker**.
- Todo corre en un solo PC (Windows 11), sin contenedores:
  - Java 21, Maven 3.9.15, Node 24 / npm 11, PostgreSQL 18 (servicio nativo
    `postgresql-x64-18`, escuchando en `localhost:5432`).
  - `psql.exe` no está en el PATH: usar la ruta completa
    `"C:\Program Files\PostgreSQL\18\bin\psql.exe"`.

## Decisiones de diseño no explícitas en el prompt

- **Maven** (no Gradle) para los 6 proyectos Java, por ser el estándar más
  consistente de mantener entre los 6 módulos.
- **Estructura de carpetas**: `backend/api-usuarios`, `backend/api-licitaciones`,
  `backend/api-ms-ventas`, `backend/api-notificaciones`, `backend/api-asistente`,
  `gateway/`, `frontend/`, `db/init.sql` en la raíz.
- **Flyway** para migraciones de esquema en cada microservicio con BD propia
  (`db/migration/V1__init.sql`), en vez de `ddl-auto: update`. Es más
  defendible para una tesis (esquema versionado y explícito).
- **Registro público con selección de rol**: el usuario pidió explícitamente
  que cualquier persona pueda registrarse eligiendo Empresa o Cliente. Se
  implementó como dos endpoints separados (`/api/auth/registro/empresa` y
  `/api/auth/registro/cliente`) en vez de un único endpoint con un campo
  "tipo" — más simple de validar (cada uno con su propio DTO y sus propios
  campos obligatorios) y igual de fácil de exponer en el frontend como un
  selector Empresa/Cliente en la misma pantalla de registro.
- **Relajación intencional de aislamiento de red** (sección 2 del prompt):
  cada microservicio (excepto el Gateway) escucha en `127.0.0.1`, no en
  `0.0.0.0`. Esto lo saca del alcance de la red local/wifi, pero sigue siendo
  alcanzable por cualquier proceso del mismo PC — a diferencia de una red de
  contenedores Docker, que sí aislaría completamente. Aceptado como trade-off
  del alcance "proyecto de título, sin Docker".
- **Endpoint interno `/internal/**`** (API Usuarios expone
  `GET /internal/usuarios/{id}`): no tiene autenticación propia (ni siquiera
  un secreto compartido). Su única protección es (a) el binding a
  `127.0.0.1` y (b) que el Gateway nunca define una ruta hacia `/internal/**`.
  Es una decisión consciente de mantener el alcance simple; documentado aquí
  para que quede explícito que no es un descuido.
- **Administrador inicial vía seed automático** (`AdminSeeder`): si al
  arrancar `api-usuarios` no existe ningún usuario con rol ADMINISTRADOR, se
  crea uno automáticamente. Credenciales por defecto:
  `admin@licitawatch.cl` / `Admin123!` (configurables con las variables de
  entorno opcionales `ADMIN_SEED_EMAIL` / `ADMIN_SEED_PASSWORD`, no incluidas
  en la lista fija de `.env` del prompt porque son opcionales con default).
- **Refresh tokens con rotación**: cada vez que se usa un refresh token para
  pedir un nuevo access token, el token usado se revoca y se emite uno nuevo
  (en vez de reutilizar el mismo hasta que expire). Se almacenan hasheados
  (SHA-256) en la tabla `refresh_token`, nunca en texto plano.
- **Rate limiting de login**: bucket4j en memoria (sin Redis), 5 intentos por
  minuto por IP de origen, solo en `POST /api/auth/login`.
- **RUT**: se valida solo el formato (`\d{7,8}-[\dkK]`), no el dígito
  verificador módulo 11 — suficiente para el alcance del MVP, evita
  sobre-ingeniería en un campo que no es el foco del proyecto.
- **CORS**: no se configura en los microservicios individuales, solo en el
  Gateway (sección 4 del prompt). Los microservicios nunca son llamados
  directamente desde el navegador.
- Tests de integración usan **H2 en memoria** (perfil de test,
  `src/test/resources/application.yml`), no la Postgres real de desarrollo —
  así los tests son reproducibles sin depender del estado de la BD local.
  Flyway se desactiva en tests; Hibernate genera el esquema desde las
  entidades (`ddl-auto: create-drop`).

## Fase 1 — API Usuarios (COMPLETO)

Puerto `8081`, bind `127.0.0.1`, BD propia `usuarios_db`.

**Implementado:**
- Modelo de datos exacto de la sección 3.1: `rol`, `usuario`, `empresa`,
  `cliente`, `administrador` (patrón usuario genérico + tabla de extensión
  por rol), más `refresh_token` (necesaria para el mecanismo de refresh/logout
  de JWT que pide la sección 6).
- Migración Flyway `V1__init.sql`. Las filas de `rol` se siembran en código
  (`RolSeeder`), no en la migración, para que el mismo seeder sirva en dev
  (Postgres) y en tests (H2).
- Registro público diferenciado: `POST /api/auth/registro/empresa`,
  `POST /api/auth/registro/cliente`.
- Login: `POST /api/auth/login` → access token (JWT, HS256, expira según
  `JWT_EXPIRATION_MINUTES`) + refresh token opaco (UUID, guardado hasheado).
- `POST /api/auth/refresh` (rota el refresh token), `POST /api/auth/logout`
  (revoca el refresh token entregado).
- Perfil propio: `GET /api/perfil`, `PUT /api/perfil/empresa`,
  `PUT /api/perfil/cliente` — la identidad se toma siempre del JWT
  (`SecurityUtils.usuarioActual()`), nunca de un parámetro de URL, así nadie
  puede leer o editar el perfil de otro usuario.
- Panel de administración (usuarios): `GET /api/admin/usuarios`,
  `GET /api/admin/usuarios/{id}`, `PUT /api/admin/usuarios/{id}/activo`,
  `POST /api/admin/usuarios/administradores` (alta de otro Administrador,
  restringido a rol ADMINISTRADOR — cumple la restricción de la sección 3.1).
- Endpoint interno para otros microservicios:
  `GET /internal/usuarios/{id}` → `{id, email, rol, nombre, activo}`.
- Seguridad: BCrypt para contraseñas, JWT firmado con `JWT_SECRET` (HMAC-SHA256),
  RBAC vía Spring Security (`hasRole`/`@PreAuthorize`), rate limiting en
  login (bucket4j, 5/min por IP), Bean Validation en todos los DTOs de
  entrada, JPA/Hibernate con consultas parametrizadas en todo (sin SQL
  concatenado en ningún punto).
- Manejador global de errores (`GlobalExceptionHandler`) → respuestas JSON
  consistentes (`ErrorResponse`) para 400/401/403/404/409/429/500.
- Tests de integración (`AuthFlowTest`, JUnit 5 + MockMvc + H2): registro,
  email duplicado, login válido/inválido, perfil autenticado/sin token,
  actualización de perfil, rotación y expiración de refresh token, RBAC de
  rutas de administración. **4/4 tests pasan** (`mvn test`).

**Cómo correrlo:**

1. Una sola vez, si no lo has hecho: crear las 4 bases de datos (pide la
   contraseña del superusuario `postgres` que definiste al instalar):
   ```
   "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -f db/init.sql
   ```
2. Levantar el servicio (carga `.env` automáticamente si usas el script de
   arranque; a mano, exportar las variables primero — ver README pendiente):
   ```
   cd backend/api-usuarios
   mvn spring-boot:run
   ```
3. Probar con curl (reemplaza los valores de ejemplo):
   ```
   curl -X POST http://127.0.0.1:8081/api/auth/registro/empresa -H "Content-Type: application/json" -d "{\"email\":\"empresa1@test.cl\",\"contrasena\":\"ClaveSegura123\",\"razonSocial\":\"Constructora Andes\",\"rut\":\"76123456-7\",\"rubro\":\"Construccion\"}"

   curl -X POST http://127.0.0.1:8081/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"empresa1@test.cl\",\"contrasena\":\"ClaveSegura123\"}"

   curl http://127.0.0.1:8081/api/perfil -H "Authorization: Bearer <accessToken>"
   ```
4. El administrador inicial queda disponible en el primer arranque:
   `admin@licitawatch.cl` / `Admin123!`.

**Verificado contra Postgres real (parcialmente) — hallazgo importante:**
- El usuario ya corrió `db/init.sql` (rol `licitawatch` + 4 bases creadas).
- Al levantar `api-usuarios` (tanto con `mvn spring-boot:run` como con el
  `.jar` empaquetado) desde las herramientas de este asistente, Flyway
  **sí conectó correctamente** a `usuarios_db` en Postgres 18.6, validó y
  confirmó el esquema (`Schema "public" is up to date`) — es decir, la
  configuración de conexión a BD, credenciales y migración son correctas.
- Sin embargo, el arranque del **Tomcat embebido falla** en el entorno de
  ejecución de las herramientas de este asistente con:
  `java.io.IOException: Unable to establish loopback connection` →
  `UnixDomainSockets.connect0` → `Invalid argument: connect`. Es un problema
  del JDK 21 al abrir el "wakeup pipe" interno de `java.nio.channels.Selector`
  vía socket de dominio Unix, dentro del árbol de procesos donde corren las
  herramientas del asistente — no es un error de la aplicación (Postgres,
  Flyway, JPA, seguridad, etc. ya se validaron correctos, y por separado los
  4 tests de integración con H2 pasan). Se probó forzar
  `-Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.WindowsSelectorProvider`
  y `-Djava.io.tmpdir=C:\Windows\Temp` vía `java -jar` directo: ninguno lo
  resolvió, porque `sun.nio.ch.PipeImpl` intenta el socket AF_UNIX
  independientemente del `SelectorProvider` elegido.
- **Pendiente de que el usuario confirme**: correr
  `mvn spring-boot:run` (con las variables de `.env` cargadas) directamente
  en su propia terminal (no a través del asistente) para descartar que sea
  una restricción específica del entorno sandbox de las herramientas del
  asistente y no de la máquina real. Si en su terminal también falla con el
  mismo stack trace, revisar: (a) actualizar a la última build de JDK 21,
  (b) que ningún antivirus/EDR esté bloqueando sockets AF_UNIX para
  `java.exe`, (c) que el driver `afunix.sys` de Windows esté habilitado.

## Fase 2 — API Licitaciones (COMPLETO)

Puerto `8082`, bind `127.0.0.1`, BD propia `licitaciones_db`.

**Decisiones de diseño adicionales de esta fase:**
- **`empresa_id` / `cliente_id` referencian `usuario.id`** (no `empresa.id` ni
  `cliente.id`), porque ese es el id que viaja en el `sub` del JWT — así
  cualquier microservicio puede comparar "¿este recurso es mío?" contra
  `SecurityUtils.usuarioActual().id()` sin tener que preguntarle nada a API
  Usuarios primero. Es el mismo criterio que anticipé en la fase 1 al decir
  que las referencias cross-servicio usan el id de `usuario`.
- **Cada microservicio valida su propio JWT de forma local** (no llama al
  endpoint de introspección de Usuarios en cada request): comparten
  `JWT_SECRET` vía `.env`, así que validar la firma localmente es válido y
  evita una llamada de red extra por cada request. El Gateway (fase 6) hará
  lo mismo. Esto implica duplicar un pequeño paquete `security/` (JwtService,
  JwtAuthenticationFilter, SecurityUtils) en cada microservicio — se aceptó
  la duplicación en vez de crear una librería compartida, porque el prompt
  enfatiza microservicios independientes y no menciona un módulo común; un
  proyecto Maven multi-módulo compartido habría acoplado el build de los 5
  servicios entre sí.
- **`GET /api/licitaciones`** siempre filtra `estado = PUBLICADA`
  (ignora cualquier intento de pedir cerradas) porque la sección 3.2 del
  prompt limita la búsqueda pública explícitamente a licitaciones
  publicadas. Para que una Empresa vea sus propias licitaciones cerradas
  existe `GET /api/licitaciones/mias`.
- **`GET /api/licitaciones/todas` (solo ADMINISTRADOR)**: no estaba en la
  lista literal de endpoints de la sección 3.2, pero el panel de
  administración (sección 1, objetivo específico 6, y slide 5 de la PPT:
  "Moderar licitaciones") sí lo requiere, y este microservicio es dueño de
  esos datos — se agregó como el mínimo necesario para que el futuro
  frontend de administración tenga de dónde leer.
- **Postulación duplicada bloqueada** (`UNIQUE (licitacion_id, cliente_id)`):
  un Cliente no puede postular dos veces a la misma licitación. No estaba
  explícito en el prompt, pero es la interpretación estándar y evita
  ambigüedad en "ver estado de mi postulación" (¿cuál de varias?).
- **Notificaciones vía `NotificacionClient`**: llamada REST síncrona
  best-effort a `POST {base-url}/notificaciones` al publicar una licitación
  y al recibir una postulación (sección 2 y 3.2). Si Notificaciones no
  responde (aún no existe, o está caída), se loguea un warning y el flujo
  principal continúa sin romperse — verificado en los tests: el warning
  aparece en el log y los tests igual pasan, porque nada en este servicio
  depende de que la llamada tenga éxito.
- **Contrato hacia Notificaciones fijado ahora**: `POST /notificaciones` con
  `{usuarioId, tipo, canal, asunto, mensaje}`. Solo `usuarioId`, `tipo` y
  `canal` se persisten en la tabla `notificacion` (coincide con el modelo de
  la sección 3.4); `asunto`/`mensaje` son campos de paso, solo para componer
  el correo real en el momento del envío. `tipo` se fija a `"LICITACION"`
  tanto para "licitación publicada" como para "postulación recibida",
  porque el modelo de datos de Notificaciones solo define dos valores de
  tipo (licitación/pago).

**Endpoints implementados:** `GET /api/licitaciones` (búsqueda pública con
filtros `rubro`/`region`), `GET /api/licitaciones/{id}`,
`GET /api/licitaciones/mias` (EMPRESA), `GET /api/licitaciones/todas`
(ADMINISTRADOR), `POST /api/licitaciones` (EMPRESA), `PUT
/api/licitaciones/{id}` (EMPRESA, dueño, solo si PUBLICADA), `PUT
/api/licitaciones/{id}/cerrar` (EMPRESA, dueño); `POST
/api/licitaciones/{id}/postulaciones` (CLIENTE), `GET
/api/licitaciones/{id}/postulaciones` (EMPRESA, dueño), `PUT
/api/licitaciones/{id}/postulaciones/{id}/estado` (EMPRESA, dueño), `GET
/api/postulaciones/mias` (CLIENTE).

**Tests:** `LicitacionFlowTest` (JUnit 5 + MockMvc + H2, JWT de prueba
firmados con el mismo secreto compartido) — **2/2 pasan**: flujo completo
publicar → buscar → postular → revisar → aceptar → cerrar → ya no aparece en
búsqueda, más RBAC (un Cliente no puede publicar), postulación duplicada
rechazada, y rutas sin token devuelven 401.

**Cómo probarlo:** igual que en la fase 1 — `mvn spring-boot:run` en
`backend/api-licitaciones` (con `.env` cargado). Como este servicio no emite
JWT, para probarlo por separado con curl necesitas primero loguearte contra
`api-usuarios` (fase 1) y usar ese `accessToken`.

**Nota de entorno (heredada de la fase 1):** no se pudo levantar el `.jar`
contra Postgres real desde las herramientas de este asistente por la misma
limitación de Tomcat/JDK descrita arriba (socket AF_UNIX). Los tests con H2
sí validan toda la lógica de negocio y seguridad.

## Fase 3 — API MS-Ventas (COMPLETO)

Puerto `8083`, bind `127.0.0.1`, BD propia `ventas_db`.

**Decisiones de diseño adicionales de esta fase:**
- **Pasarela de pago real conectada**: se usó el SDK oficial
  `com.github.transbankdevelopers:transbank-sdk-java:6.2.1` (confirmado en
  Maven Central — ojo, no es el groupId `cl.transbank.sdk` que uno esperaría
  por el nombre; el proyecto se mudó a GitHub Packages/`com.github.*`). Se
  verificó la API real del SDK (clases `WebpayPlus.Transaction`,
  `WebpayPlusTransactionCreateResponse`, `WebpayPlusTransactionCommitResponse`)
  leyendo el código fuente del repo oficial antes de escribir el adapter, en
  vez de asumir una API por memoria.
- **`PasarelaPagoAdapter`** (paquete `pago/`): interfaz con `iniciar(...)` y
  `confirmar(...)`; `TransbankWebpayAdapter` es la única implementación real,
  tal como pide el prompt ("intercambiable por si más adelante cambio a
  Flow"). Los tests usan una `FakePasarelaPagoAdapter` (`@Primary`,
  registrada con `@Import` porque `@TestComponent` no entra al scan normal
  de `@SpringBootTest`) para no depender de la red ni del sandbox real de
  Transbank en cada corrida de tests.
- **Suscripcion sin estado "pendiente"**: el modelo de datos original solo
  define 2 estados (activa/vencida). Al iniciar una compra se crea la
  `Suscripcion` en `VENCIDA` (fecha_inicio/vencimiento en null) — recién pasa
  a `ACTIVA` cuando `confirmar()` recibe una respuesta aprobada de Transbank.
  Cada compra crea una fila de `Suscripcion` nueva (no se reutiliza una
  existente), para que el historial de contrataciones quede completo.
- **Vencimiento perezoso, sin scheduler**: no hay ningún job en segundo plano
  marcando suscripciones vencidas. Cada vez que se consulta
  `GET /api/suscripciones/mia` (o el endpoint interno), si la suscripción
  está `ACTIVA` pero su `fecha_vencimiento` ya pasó, se recalcula a `VENCIDA`
  y se persiste en ese mismo momento. Evita añadir infraestructura de
  scheduling para el alcance del MVP.
- **Columna `pago.token`** (no estaba en el modelo mínimo de la sección 3.3):
  imprescindible porque Transbank identifica la transacción por su `token`,
  no por nuestro `id_transaccion` (que es el `authorizationCode`, y solo
  existe después de confirmar). `id_transaccion` sigue siendo el campo que
  pedía el modelo original.
- **Flujo de redirección real de Webpay Plus**: `create()` devuelve
  `{token, url}`, y el frontend (fase 6) deberá armar un formulario HTML que
  haga **POST** a `url` con un campo oculto `token_ws=token` — no es un
  simple redirect GET con query string. Lo dejo anotado en
  `IniciarVentaResponse` para que no se pierda al construir el frontend.
- **`confirmar` es idempotente**: si el pago ya fue procesado (no está
  `PENDIENTE`), no se vuelve a llamar a Transbank — se devuelve el resultado
  ya guardado. Esto es necesario porque la pantalla `/pago/resultado` del
  frontend puede llamarlo más de una vez (recarga de página).
- **Catálogo de planes fijo, sembrado por código** (`PlanSeeder`): Estándar
  (con límites mensuales de publicación/postulación) y Premium (ilimitado),
  igual que en la PPT (slide 6). No hay endpoint para crear/editar planes —
  el catálogo es parte del diseño, no un dato administrable en este MVP.
  Precios de referencia (`$9.990` / `$24.990`) son un valor de ejemplo, no
  vinieron especificados.
- **Los límites de plan (5 publicaciones/mes, etc.) no se aplican todavía en
  API Licitaciones**: MS-Ventas expone el estado de la suscripción
  (`GET /internal/suscripciones/{usuarioId}`) para que otros servicios lo
  consuman "si corresponde" (tal como dice la sección 3.3), pero hacer
  cumplir el límite en el momento de publicar/postular no estaba en la lista
  explícita de endpoints y se dejó fuera de alcance para no tener que volver
  a tocar Licitaciones a mitad de esta fase. Si se quiere aplicar de verdad,
  es un cambio acotado en `LicitacionService`/`PostulacionService` que
  consulte ese endpoint interno antes de crear.

**Endpoints implementados:** `GET /api/planes`, `POST /api/ventas/iniciar`,
`POST /api/ventas/confirmar`, `GET /api/ventas/todas` (ADMINISTRADOR),
`GET /api/suscripciones/mia`, `GET /internal/suscripciones/{usuarioId}`.

**Tests:** `VentaFlowTest` (JUnit 5 + MockMvc + H2 + Transbank fake) —
**5/5 pasan**: catálogo de planes, pago aprobado activa la suscripción
(incluye verificar que confirmar es idempotente), pago rechazado no activa
nada, un usuario no puede confirmar el pago de otro (403), y
`GET /api/ventas/todas` es solo para ADMINISTRADOR.

**Cómo probarlo:** `mvn spring-boot:run` en `backend/api-ms-ventas` (con
`.env` cargado). El pago real contra el sandbox de Transbank solo puede
probarse end-to-end una vez exista el frontend (fase 6) para completar el
formulario de tarjeta en la página hospedada de Webpay — por ahora, los
tests cubren toda la lógica de negocio con un adapter falso.

**Nota de entorno (heredada):** mismo problema de Tomcat/JDK del entorno de
este asistente para levantar el `.jar` real; no aplica a los tests (H2).

## Fase 4 — API Notificaciones (COMPLETO)

Puerto `8084`, bind `127.0.0.1`, BD propia `notificaciones_db`.

**Decisiones de diseño adicionales de esta fase:**
- **Sin Spring Security ni JWT en este servicio**: a diferencia de los otros
  3, Notificaciones no tiene ningún endpoint pensado para el frontend ni
  para un usuario final — es puramente servicio-a-servicio. Su única
  protección es el binding a `127.0.0.1` y que el Gateway nunca define ruta
  hacia él (igual que `/internal/**` en los demás). Se dejó fuera la
  dependencia de seguridad por completo, para que el módulo sea tan simple
  como pide la sección 3.4 ("no hay lógica de negocio adicional, mantenlo
  simple a propósito").
- **Llamada a `GET /internal/usuarios/{id}` en API Usuarios**: no está
  dibujada como flecha en el diagrama de arquitectura de la sección 2, pero
  es imprescindible — la tabla `notificacion` solo guarda `usuario_id`
  (sección 3.4), nunca el email, así que este servicio necesita resolverlo
  antes de poder mandar cualquier correo. Es una conexión REST más, agregada
  por necesidad práctica y documentada aquí para que no parezca un
  descuido.
- **Solo 2 estados (`ENVIADO`/`PENDIENTE`), sin un tercer estado
  "fallido"**: si el intento de envío falla (SMTP caído, límite de Gmail
  alcanzado, o no se pudo resolver el email del usuario), la notificación
  queda en `PENDIENTE` y se loguea el motivo — no hay reintentos
  automáticos ni un job que los reprocese (no estaba pedido, y agregarlo
  habría requerido un scheduler). El requisito central de la sección 3.4 sí
  se cumple estrictamente: **ningún fallo de correo devuelve error al
  llamador** — el endpoint de ingesta siempre responde 201, confirmado en
  el test `NotificacionFallbackTest` (sin mocks: ni Usuarios ni el SMTP
  están arriba, y aun así responde 201 con estado `PENDIENTE`).
- **`asunto`/`mensaje` no se persisten**: solo viajan en el request de
  ingesta para componer el correo en el momento; la tabla `notificacion`
  respeta el esquema mínimo de la sección 3.4 (`id, usuario_id, tipo, canal,
  estado, fecha`).
- **SMTP vía `JavaMailSender`** (Gmail, STARTTLS, puerto 587, contraseña de
  aplicación ya provista en `.env`) con timeouts cortos (5s) para no dejar
  la request colgada si Gmail no responde.

**Endpoint implementado:** `POST /notificaciones` (único, según lo pedido).

**Tests:** `NotificacionFlowTest` (con `UsuarioClient`/`EmailService`
mockeados, para probar los caminos ENVIADO/PENDIENTE de forma determinista y
validación 400) + `NotificacionFallbackTest` (sin mocks, contra un `Usuarios`
y un SMTP que no existen, para probar la resiliencia real) — **4/4 pasan**.

**Cómo probarlo:** `mvn spring-boot:run` en `backend/api-notificaciones` (con
`.env` cargado). Para ver un correo real llegar hay que tener `api-usuarios`
corriendo también (para resolver el email) y un usuario ya registrado; se
prueba con curl directo a `POST http://127.0.0.1:8084/notificaciones`.

**Nota de entorno (heredada):** mismo problema de Tomcat/JDK del entorno de
este asistente para levantar el `.jar` real; no aplica a los tests (H2 +
mocks/beans reales de red apuntando a puertos que no existen).

## Fase 5 — API Asistente / LicitAsist (COMPLETO)

Puerto `8085`, bind `127.0.0.1`, **sin base de datos propia** (por diseño —
sección 2 y 3.5). Último microservicio antes del Gateway.

**Decisiones de diseño adicionales de esta fase:**
- **Sin motor de clasificación de intención**: en vez de intentar detectar
  con reglas si el usuario pregunta por "resumen de postulaciones" vs.
  "comparar planes" vs. "licitaciones por vencer", se arma **un mismo bloque
  de contexto real por rol** (perfil, plan vigente, y según el rol:
  licitaciones propias para Empresa, o postulaciones propias + licitaciones
  publicadas para Cliente) y se deja que el propio modelo de Groq decida qué
  parte usar para responder la pregunta puntual. Es el patrón estándar para
  este tipo de asistente "RAG ligero", y evita un clasificador de intenciones
  frágil que de todas formas el LLM resuelve mejor teniendo el contexto
  correcto a la vista. Documentado en el javadoc de `ContextoService`.
- **El JWT del usuario se reenvía tal cual** a `API Licitaciones` para pedir
  `/api/licitaciones/mias`, `/api/postulaciones/mias` y `/api/licitaciones`
  — así Licitaciones aplica exactamente la misma autorización que si el
  usuario hubiera llamado a esos endpoints directamente (una Empresa jamás
  puede terminar viendo licitaciones de otra vía el chat). En cambio, para
  `API Usuarios` y `API MS-Ventas` se usan sus endpoints `/internal/**` (sin
  JWT), porque ya identifican al usuario por `usuarioId` en la URL.
- **No se implementaron todas las capacidades "de ejemplo" de la PPT al pie
  de la letra** (p. ej. "avisa licitaciones por vencer *sin postulaciones*"
  necesitaría el conteo de postulaciones por licitación, que exigiría N
  llamadas adicionales por licitación): se pasa la lista de licitaciones
  propias con su `fechaCierre`, y el modelo puede razonar sobre cuáles
  cierran pronto, pero no sabe cuántas postulaciones tiene cada una a menos
  que el usuario pregunte por una en particular. Es una simplificación de
  alcance consciente, no un olvido — evita hacer *fan-out* de llamadas REST
  por cada licitación en cada mensaje del chat.
- **Manejo del Free Tier de Groq**: `GroqClient` distingue explícitamente un
  429 (límite alcanzado → mensaje claro al usuario, sin reintentar) de
  cualquier otro error (hasta 2 intentos, después "asistente no disponible
  por ahora"). Nunca se presenta como servicio ilimitado, tal como pide la
  sección 3.5.
- **Contador de uso en memoria** (`UsoGroqContador`): global y por usuario,
  expuesto en `GET /api/asistente/uso` y en cada respuesta del chat
  (`usoDelUsuario`). Sin persistencia ni alertas automáticas — cumple el
  paso 11 del flujo técnico sin construir infraestructura de monitoreo
  adicional para el alcance del MVP.
- **Memoria conversacional 100% en proceso** (sección 8.1): mapa
  `usuario_id -> últimos 10 turnos`, expira a los 30 minutos de inactividad
  (revisado de forma perezosa, sin scheduler). Se pierde si el servicio se
  reinicia — es la consecuencia aceptada y documentada de que este
  microservicio no tiene base de datos propia, no un olvido.
- **Visible solo para Empresa y Cliente**: `@PreAuthorize("hasAnyRole('EMPRESA','CLIENTE')")`
  a nivel de controlador — un Administrador autenticado recibe 403 si intenta
  usar el chat, coherente con la sección 5.2 ("widget visible para Empresa y
  Cliente").

**Endpoints implementados:** `POST /api/asistente/chat`,
`GET /api/asistente/uso`.

**Tests:** `ChatFlowTest` (con `GroqClient` mockeado; los clientes de
contexto son reales, apuntando a puertos donde no hay nada corriendo, para
probar la resiliencia real del armado de contexto) — **5/5 pasan**: uso que
se incrementa por mensaje, el historial de conversación efectivamente viaja
en la segunda llamada a Groq (verificado inspeccionando los mensajes
capturados), un Administrador no puede usar el chat (403), sin token → 401,
y un 429 de Groq se traduce en un 429 con mensaje claro para el usuario.

**Cómo probarlo:** `mvn spring-boot:run` en `backend/api-asistente` (con
`.env` cargado, incluye la `GROQ_API_KEY` real). Para una prueba end-to-end
real hace falta un JWT válido (de `api-usuarios`) y, para que el contexto no
esté vacío, tener `api-usuarios`, `api-licitaciones` y `api-ms-ventas`
corriendo también.

**Nota de entorno (heredada):** mismo problema de Tomcat/JDK del entorno de
este asistente para levantar el `.jar` real; no aplica a los tests.

## Fase 6 — API Gateway (COMPLETO)

Puerto `8080`. Es el único proceso que NO fija `server.address` a
`127.0.0.1` — a propósito (sección 2 del diseño: es el único componente
pensado para ser alcanzable desde fuera de este mismo proceso).

**Decisiones de diseño adicionales de esta fase:**
- **Spring Cloud Gateway** (reactivo, WebFlux) con **Spring Cloud
  2023.0.6** (el BOM compatible con Spring Boot 3.3.4) — confirmado en
  Maven Central antes de fijar la versión, igual que con el SDK de
  Transbank.
- **Toda la lógica de auth/RBAC/rate-limit vive en un único `GlobalFilter`**
  (`GatewayAuthFilter`), no en Spring Security reactivo: para un gateway que
  solo necesita "validar JWT + mirar un claim de rol + contar requests", un
  `SecurityWebFilterChain` completo habría sido más ceremonia que valor. El
  filtro:
  - Deja pasar sin JWT únicamente `/api/auth/registro/empresa`,
    `/api/auth/registro/cliente`, `/api/auth/login`, `/api/auth/refresh`,
    `/api/auth/logout` (las únicas rutas del sistema donde, por definición,
    todavía no puede existir un token).
  - Para todo lo demás, valida la firma del JWT localmente (mismo
    `JWT_SECRET` compartido) y exige rol `ADMINISTRADOR` en `/api/admin/**`
    (Usuarios) y en `/api/licitaciones/todas` / `/api/ventas/todas` (las
    rutas de panel de administración que viven en otros microservicios).
  - Aplica rate limiting con bucket4j en memoria: por IP en
    `POST /api/auth/login`, por usuario (del propio JWT) en
    `/api/asistente/**`.
  - La autorización fina (¿es dueño del recurso?) se deja a cada
    microservicio, tal como pide la sección 4 — el Gateway solo hace la
    capa gruesa (autenticado sí/no, rol admin sí/no).
- **`/internal/**` de cada microservicio nunca tiene ruta definida** en
  `application.yml`: no hace falta bloquearlo explícitamente en el filtro,
  Spring Cloud Gateway responde 404 por su cuenta para cualquier path sin
  predicate que lo matchee. Es la forma más simple de garantizar "no
  expuesto públicamente por el Gateway".
- **CORS restrictivo vía `spring.cloud.gateway.globalcors`**: origen
  explícito `http://localhost:5173`, nunca `*` (sección 6, checklist de
  seguridad #1).
- **Limitación de entorno encontrada y resuelta para los tests**: el
  entorno donde corren las herramientas de este asistente no puede levantar
  un servidor Netty embebido (mismo problema de fondo que con Tomcat en las
  fases 1-5: el JDK no logra abrir el "wakeup pipe" interno de
  `java.nio.channels.Selector` — ver notas de entorno de fases anteriores).
  En vez de insistir con un test de integración `WebTestClient` contra un
  servidor real, se testeó `GatewayAuthFilter` de forma aislada (un
  `ServerWebExchange` simulado + una `GatewayFilterChain` de prueba, sin
  Spring context ni servidor de por medio) — más rápido y en realidad la
  forma más estándar de testear un `GlobalFilter` individual. Lo único que
  queda sin cubrir por un test automatizado es el 404 de Spring Cloud
  Gateway para rutas sin predicate (`/internal/**`): es comportamiento del
  framework, no código propio, y está garantizado por no declarar esas
  rutas.

**Rutas configuradas:** `/api/auth/**`, `/api/perfil/**`,
`/api/admin/usuarios/**` → API Usuarios; `/api/licitaciones/**`,
`/api/postulaciones/**` → API Licitaciones; `/api/planes/**`,
`/api/ventas/**`, `/api/suscripciones/**` → API MS-Ventas;
`/api/asistente/**` → API Asistente.

**Tests:** `GatewayFilterTest` (unitario, sin contexto Spring) —
**7/7 pasan**: 401 sin token, 403 en ruta admin sin rol, una request con rol
ADMINISTRADOR sí continúa la cadena, `/api/licitaciones/todas` respeta el
mismo RBAC aunque pertenece a otro microservicio, rate limit de login por
IP, rate limit de LicitAsist por usuario, y las 4 rutas públicas de auth no
piden token.

**Cómo probarlo:** con los 5 microservicios y el Gateway corriendo (`mvn
spring-boot:run` en `gateway/`, con `.env` cargado), probar end-to-end con
curl/Postman contra `http://localhost:8080` en vez de los puertos
individuales — por ejemplo, registrar una empresa, loguearse, y usar el
`accessToken` contra `http://localhost:8080/api/perfil`.

**Nota de entorno (heredada, y la razón por la que no verifiqué el
enrutamiento real end-to-end todavía):** el mismo problema de Tomcat/JDK que
impide levantar los otros 5 servicios desde las herramientas de este
asistente aplica también al Gateway (con Netty en vez de Tomcat). Falta que
el usuario confirme, en su propia terminal, que el Gateway enruta
correctamente a los 5 servicios reales — la lógica de auth/RBAC/rate-limit
ya está probada de forma aislada y exhaustiva.

## Fase de verificación end-to-end (post-fase 6) — 2 bugs reales encontrados y corregidos

El usuario levantó los 6 procesos con `start-all.ps1` en su propia máquina (en
su entorno SÍ arrancan sin el problema de Tomcat/Netty descrito en fases
anteriores — esa limitación resultó ser específica del entorno donde corren
las herramientas de este asistente, no de la máquina real) y reportó 500 en
`/api/auth/registro/empresa` y `/api/auth/login`, incluso contra
`localhost:8081` directo.

**Investigación:** conectado directamente a la Postgres real del usuario
(credencial de la app, no la de superusuario) y llamando a los servicios ya
corriendo como cliente HTTP (mismo `localhost`, por eso sí fue alcanzable
aunque este asistente no pueda *levantar* sus propios servidores aquí):

1. **Roles y admin sembrados correctamente** en `usuarios_db` (3 roles, 1
   administrador) — la inicialización de datos funciona bien.
2. **Registro y login funcionan perfectamente** con un JSON bien formado
   (probado end-to-end, incluyendo la emisión real de un JWT).
3. El único error real en el log (`logs/api-usuarios.out.log`) era un
   `HttpMessageNotReadableException` — JSON malformado en el cuerpo de la
   petición (típico al pasar comandos `curl` estilo Bash/cmd a PowerShell,
   donde el escapado de comillas no es el mismo).

**Bug real encontrado — Bug #1 (los 5 microservicios Spring MVC):**
`GlobalExceptionHandler` no tenía un handler específico para
`HttpMessageNotReadableException` (JSON malformado) ni para
`MethodArgumentTypeMismatchException` (parámetro con tipo inválido), así que
ambos caían en el `catch-all` de `Exception.class` y devolvían **500** en
vez de **400**. Es un bug real (un error de formato del cliente nunca debería
verse como un error del servidor), aunque no el que el usuario sospechaba
(no hay nada mal en la lógica de negocio, la BD, ni la seguridad). Corregido
agregando ambos handlers explícitos en los 5 `GlobalExceptionHandler.java`
(Usuarios, Licitaciones, MS-Ventas, Notificaciones, Asistente), con un test
de regresión nuevo en cada suite (`cuerpoConJsonMalformadoDevuelve400YNo500`)
— **las 5 suites completas vuelven a pasar** (5+3+6+5+6 = 25 tests).

**Bug real encontrado — Bug #2 (Gateway, no relacionado con el reporte
original, encontrado al intentar verificar el flujo vía Gateway):** cada
request proxied a través del Gateway fallaba con
`java.lang.NoSuchMethodError: HttpHeaders.headerSet()`. Causa raíz:
`spring-cloud-gateway-server:4.1.9` (resuelto desde el BOM
`spring-cloud-dependencies:2023.0.6`) llama a un método que **solo existe en
Spring Framework 7** — un problema real de compatibilidad entre esa versión
de Spring Cloud y Spring Boot 3.3.4 (que trae Spring Framework 6.1.13, igual
que los otros 5 servicios). Verificado con `javap` sobre los `.jar`
descargados: `headerSet()` no existe en `spring-web:6.1.13`, solo en
`spring-web:7.0.9`. Corregido bajando el BOM de Spring Cloud a **2023.0.3**
(resuelve `spring-cloud-gateway-server:4.1.5`, que no referencia ese
método — confirmado también con `javap`), sin tocar la versión de Spring
Boot del Gateway ni de ningún otro servicio. Recompilado y los 7 tests del
Gateway (`GatewayFilterTest`) siguen pasando.

**Pendiente de que el usuario confirme:** este asistente no pudo reiniciar
el proceso del Gateway que el propio usuario ya tenía corriendo — ni
deteniéndolo (el proceso pertenece a la sesión de terminal del usuario, no a
la de este asistente: `Stop-Process` devolvió "Acceso denegado" al intentarlo
desde aquí) ni levantando uno nuevo en su reemplazo (mismo problema de
sandbox de NIO/Netty que impide a este asistente levantar servidores propios,
descrito en fases anteriores). **El usuario debe reiniciar el Gateway él
mismo** (`.\stop-all.ps1` seguido de `.\start-all.ps1`, o solo detener y
volver a levantar el proceso de `gateway/`) para que tome la corrección.

**Bug real encontrado — Bug #3 (config, no código): Groq retiró los modelos
Llama por completo.** Al probar `POST /api/asistente/chat` vía Gateway,
`api-asistente` devolvió 502. El log mostró la causa exacta: Groq respondió
`404 model_not_found` para `llama-3.3-70b-versatile` — confirmado además
contra `GET {GROQ_API_BASE_URL}/models` con la API key real: **Groq ya no
sirve ningún modelo Llama** (ni siquiera aparece en la lista de modelos
disponibles), y su propia documentación recomienda `openai/gpt-oss-120b`
como reemplazo para uso general. Esto es exactamente el riesgo que el
prompt original anticipó ("Groq actualiza/retira modelos con el tiempo") —
por eso, siguiendo la instrucción de no desviarse en silencio, se consultó
al usuario antes de cambiar `GROQ_MODEL`. **Decisión confirmada por el
usuario:** `GROQ_MODEL=openai/gpt-oss-120b` (ya actualizado en `.env`).
Esto es una desviación real de la arquitectura original ("Groq API, modelo
Llama" → ya no es posible, Groq no ofrece Llama) impuesta por el proveedor
externo, no una decisión de diseño propia. Pendiente: el usuario debe
reiniciar `api-asistente` para que tome el nuevo valor (Spring solo lee
`.env` al arrancar).

**Verificación final (con los 6 procesos reiniciados por el usuario y
`GROQ_MODEL=openai/gpt-oss-120b`):** flujo completo probado vía Gateway
(`http://localhost:8080`) de punta a punta — registro Empresa y Cliente,
login, publicar licitación, buscar licitaciones, postular, ver catálogo de
planes, RBAC (Cliente rechazado en `/api/admin/usuarios` con 403),
`/internal/**` bloqueado por el Gateway (404), y LicitAsist respondiendo
correctamente con `openai/gpt-oss-120b`. Se verificó además, capturando los
bytes crudos de la respuesta, que el backend efectivamente envía UTF-8
correcto (`ó` → `C3 B3`) — un texto con acentos que se veía mal
(`gestiÃ³n`) en la consola era un problema de decodificación de
`Invoke-RestMethod` en Windows PowerShell 5.1, no un bug del servidor.
**Los 6 procesos y el flujo end-to-end completo quedan confirmados
funcionando.**

**Dato de prueba dejado en la BD real durante la investigación:** un usuario
Empresa de prueba (`debug-test@test.cl`, RUT `11111111-1`) quedó creado en
`usuarios_db` al verificar que el registro funcionaba. Es inofensivo (datos
de desarrollo local), pero queda anotado por transparencia — se puede borrar
a mano o dejar, no afecta nada.

## Fase 7 — Frontend (COMPLETO)

React 18 + Vite + TypeScript + Tailwind CSS, puerto `5173`. Diseño oscuro
"tecnológico" (glassmorphism, gradientes índigo/violeta, Inter) a pedido
explícito del usuario.

**Decisiones de diseño de esta fase:**
- **Cliente HTTP centralizado** (`src/api/client.ts`, Axios): interceptor de
  request agrega el JWT desde el store; interceptor de response detecta 401,
  llama a `POST /api/auth/refresh` una sola vez (compartida entre requests
  concurrentes vía una promesa en curso) y reintenta la request original — si
  el refresh también falla, cierra la sesión. Cumple literalmente "maneja el
  refresh/expiración" de la sección 5.1.
- **Zustand con `persist`** (localStorage) para el estado de auth — más
  liviano que Context aquí porque el cliente HTTP necesita leer el token
  fuera de React (en el interceptor), y Zustand expone `getState()` sin
  hooks. Sin Redux, tal como sugería el prompt.
- **`frontend/.env` propio**: Vite no lee el `.env` de la raíz del proyecto
  (solo lee el de su propia carpeta) — se creó `frontend/.env` con
  `VITE_API_BASE_URL`, git-ignorado igual que el resto.
- **LicitAsist como widget flotante global** (`ChatWidget.tsx`): burbuja con
  glow pulsante, panel con historial visible mientras dura la sesión de
  React (no se resetea al navegar entre páginas, solo con un F5 completo —
  coherente con que el backend tampoco persiste el historial), sugerencias
  rápidas, indicador de "escribiendo", y el disclaimer obligatorio de la
  sección 5.2 (es IA, no una persona; depende de un servicio externo con
  límites de uso). Visible solo para EMPRESA y CLIENTE, nunca para
  ADMINISTRADOR.
- **Flujo real de Webpay Plus resuelto con un plugin de Vite**
  (`webpayReturnBridge` en `vite.config.ts`): Webpay vuelve al `returnUrl`
  con un **POST**, no un GET, y una SPA no puede leer el cuerpo de un POST
  después de que el navegador ya completó esa navegación. El plugin
  intercepta ese POST en el dev server, extrae `token_ws` (o la señal de
  cancelación `TBK_TOKEN`) y redirige (303) a la misma ruta como GET con el
  dato en el query string, que sí es legible por React. **Esto solo
  funciona en `npm run dev`** — un despliegue real necesitaría un endpoint
  de servidor equivalente; está fuera de alcance porque el proyecto entero
  es 100% local (`irAWebpay()` en `src/utils/webpay.ts` arma el formulario
  HTML de ida con el `token_ws` oculto, como exige Webpay Plus).
- **RBAC también en el router** (`ProtectedRoute` con `rolesPermitidos`):
  además de que cada backend ya valida el rol, las rutas de React Router
  redirigen si el rol no corresponde — capa extra de UX, no de seguridad
  real (la seguridad real vive en el Gateway y en cada microservicio).

**Verificado en vivo** (con el navegador de este asistente contra los
servicios reales del usuario, ya reiniciados con las correcciones de la
fase de verificación anterior): login, listado de licitaciones con datos
reales, chat de LicitAsist respondiendo con datos reales de la cuenta
(confirmando además, de forma honesta, cuando no tenía un dato en su
contexto en vez de inventarlo), panel de administración con conteos reales,
tabla de usuarios.

## Corrección post-verificación: Estándar es gratuito por defecto

El usuario aclaró explícitamente: **el plan Estándar no se paga, viene
activo por defecto en toda cuenta nueva — solo Premium se contrata vía
Webpay.** Esto no estaba resuelto así en el MVP original (ambos planes
tenían precio y pasaban por Transbank). Cambios en `api-ms-ventas`:

- `PlanSeeder`: precio de ESTANDAR ahora `0` (antes `$9.990`).
- **Migración Flyway `V2__plan_estandar_gratuito.sql`**: corrige el precio
  en instalaciones que ya habían sembrado el plan con el precio antiguo
  (como la del usuario) — se aplica sola la próxima vez que arranque el
  servicio, sin pasos manuales.
- `SuscripcionService.obtenerVigente()`: si el usuario no tiene ninguna
  suscripción de pago actualmente `ACTIVA` (nunca compró Premium, o su
  Premium venció), devuelve un Estándar `ACTIVA` "virtual" sin necesitar
  una fila en la base de datos — nadie tiene que "comprar" el plan
  gratuito. De paso, se corrigió para que la suscripción vigente se elija
  entre TODAS las activas del usuario (la de mayor vencimiento), no solo la
  más reciente por fecha de creación — evita que un intento de pago fallido
  posterior a una compra exitosa "tape" la suscripción real.
- `VentaService.iniciar()`: rechaza con 409 (`PlanGratuitoException`) si se
  intenta "comprar" un plan con precio 0 — defensa en profundidad además del
  frontend, que ya deshabilita el botón "Contratar" para el plan gratuito.
- Frontend (`PlanesPage.tsx`): el plan con precio 0 muestra "Gratis" en vez
  de "$0", botón deshabilitado con etiqueta "Plan gratuito", y el aviso de
  plan vigente ya no asume que siempre hay una fecha de vencimiento.
- Tests de `VentaFlowTest` actualizados para obtener el id de PREMIUM
  dinámicamente (antes asumían que `planId=1` servía para probar el flujo
  de pago) + nuevo test `suscripcionPorDefectoEsEstandarGratisYNoRequierePago`.
  **7/7 pasan.**

## Mejora relacionada: LicitAsist ahora conoce el catálogo de planes

Al probar el chat en vivo, LicitAsist respondió honestamente que no tenía
los detalles de los planes en su contexto (correcto: `ContextoService`
nunca los pedía) — pero la sección 3.5 del prompt sí lista explícitamente
"comparar los planes según su uso" y "aclarar diferencias entre planes"
como capacidades de LicitAsist. Se agregó `VentaContextoClient.obtenerCatalogoPlanes()`
(reenvía el JWT del usuario a `GET /api/planes`, que sí exige autenticación)
y `ContextoService` ahora siempre incluye el catálogo completo en el
contexto, para ambos roles. **6/6 tests de `api-asistente` siguen pasando.**

## Bug real encontrado probando un pago real con tarjeta de prueba

El usuario probó el flujo completo de Webpay Plus con la tarjeta de prueba
del sandbox. El pago se aprobó (su plan quedó en Premium, confirmado en
`/planes`), pero `/pago/resultado` igual mostró "No se pudo confirmar el
pago con la pasarela".

**Causa raíz** (confirmada en `logs/api-ms-ventas.out.log`):
`TransbankHttpApiException: Transaction already locked by another process |
code: 422`. React 18 **StrictMode** (activo en `main.tsx`, solo afecta modo
desarrollo) invoca cada efecto dos veces a propósito para detectar efectos
no idempotentes — el `useEffect` de `PagoResultadoPage` llamaba a
`confirmarPago(tokenWs)` sin ninguna guarda, así que se disparó dos veces
casi simultáneamente. La primera llamada llegó a Transbank, se aprobó y
activó la suscripción correctamente. La segunda llamada, casi en paralelo,
alcanzó a leer el `Pago` todavía en `PENDIENTE` (la primera no había
terminado de escribir el resultado) y también intentó confirmar con
Transbank — que correctamente rechazó ese segundo intento simultáneo sobre
la misma transacción. La pantalla terminó mostrando el resultado de la
promesa que resolvió al final (la del error), aunque el pago real ya
estaba aprobado. **No hubo cobro doble ni ningún dato inconsistente** —
Transbank solo cobra una vez independientemente de esto.

**Corregido en ambos lados** (el problema real era una condición de carrera
en el backend; el de React era el disparador más probable, pero cualquier
doble llamada legítima —doble clic, refrescar la página en el momento
exacto— habría producido lo mismo):
- **Backend** (`PagoRepository`): nuevo `findByTokenParaActualizar()` con
  `@Lock(LockModeType.PESSIMISTIC_WRITE)` — la fila del `Pago` queda
  bloqueada mientras dura la transacción de `confirmar()`, así que una
  segunda llamada concurrente espera a que la primera termine y, al releer,
  ve que el pago ya no está `PENDIENTE` y devuelve el resultado ya
  guardado en vez de volver a llamar a Transbank. `VentaService.confirmar()`
  actualizado para usar este metodo. **7/7 tests siguen pasando** (H2
  soporta el lock pesimista sin problema).
- **Frontend** (`PagoResultadoPage.tsx`): un `useRef` evita que el segundo
  montaje de StrictMode repita la llamada de red en primer lugar — evita el
  problema en el origen, y de paso ahorra una llamada innecesaria a la
  pasarela en cada carga de esta página durante desarrollo.

## Diagnóstico: "no me llegó ningún correo"

El usuario reportó que ni la confirmación del pago Premium ni la
postulación le habían mandado un correo real. Investigación (con acceso
directo a los logs y a la BD real, y probando la ingesta de notificaciones
en vivo):

1. **El pipeline de correo SÍ funciona**: se mandaron dos correos de prueba
   reales por SMTP directamente contra `POST /notificaciones` (uno al admin
   sembrado, uno a la cuenta real del usuario) y ambos llegaron.
2. **Los 6 procesos se reiniciaron varias veces durante esta sesión de
   depuración** (confirmado comparando la hora de inicio de cada proceso
   contra la hora de los eventos reales de pago/postulación) — la
   explicación más probable de los 2 correos faltantes es que
   `api-notificaciones` u otro servicio involucrado no estaba arriba (o
   estaba a mitad de un reinicio) justo en el momento de esas dos acciones
   puntuales, no un bug de código.
3. **Bug real encontrado de todas formas**: el timeout de lectura del
   `RestClient` que usa `NotificacionClient` en `api-licitaciones` y
   `api-ms-ventas` estaba en **3 segundos**, pero un envío real de correo
   por Gmail (conectar + STARTTLS + autenticar + enviar) toma en la
   práctica **3.3-3.7 segundos** — es decir, el llamador se rendía justo
   antes (o casi al mismo tiempo) de que Notificaciones terminara de
   verdad. Esto no rompía el flujo principal (es una llamada best-effort),
   pero sí generaba un warning de "no se pudo notificar" incluso cuando el
   correo probablemente se enviaba bien de todas formas. **Corregido**:
   timeout de lectura subido a 10s en ambos servicios (`RestClientConfig`).

## Mejora pedida: correo de confirmación de pago más completo

El usuario pidió que el correo de confirmación de pago incluya el plan
comprado, el monto y el detalle de la tarjeta, "estilo empresa". Cambios en
`api-ms-ventas`:

- `ConfirmacionPagoResultado` ahora incluye `ultimosDigitosTarjeta`, tomado
  de `CardDetail.getCardNumber()` del SDK de Transbank — **este campo ya
  viene enmascarado por Transbank** (nunca se recibe, ni se guarda, el
  número completo de la tarjeta; sigue cumpliéndose la sección 5.3 del
  diseño al pie de la letra).
- `VentaService.confirmar()` arma un correo de confirmación con: plan
  contratado, monto pagado, vigencia (desde/hasta), código de autorización
  de Webpay y los últimos dígitos de la tarjeta. Para el rechazo, un
  mensaje más claro también.
- Se le envió manualmente al usuario, con sus datos reales de compra
  (Premium, $24.990, vigente 2026-09-10 a 2026-10-10, autorización 1213,
  tomados directo de `ventas_db`), el mismo contenido que el código nuevo
  generará automáticamente en la próxima compra real. Los últimos dígitos
  de tarjeta no se incluyeron en ese envío puntual porque esa compra ya
  había ocurrido antes de este cambio y ese dato no se guardó en su
  momento — las compras futuras sí lo incluirán.
- `FakePasarelaPagoAdapter` (test double) actualizado para el nuevo campo.
  **7/7 tests de `api-ms-ventas` siguen pasando.**

**Nota importante — pedido fuera del alcance actual, no implementado sin
confirmar:** el usuario también mencionó un correo de "confirmación de
registro". El diseño original (sección 3.4) solo define 3 eventos que
disparan una notificación: licitación publicada, postulación recibida, pago
confirmado — el registro de una cuenta **no** es uno de ellos, y agregarlo
significaría que API Usuarios tendría que empezar a llamar a API
Notificaciones (una conexión nueva no contemplada en el diagrama de
arquitectura original). No se agregó en silencio; si se quiere, es un
cambio acotado y queda pendiente de que el usuario lo pida explícitamente.

**Pendiente de que el usuario reinicie** (ninguno de estos cambios toma
efecto hasta reiniciar el proceso correspondiente; el cambio de frontend ya
está activo solo con el hot-reload de Vite, sin reiniciar nada):
- `api-ms-ventas`: plan Estándar gratuito + suscripción por defecto +
  corrección de la condición de carrera al confirmar un pago + correo de
  confirmación con detalle completo + timeout de notificación más generoso.
- `api-licitaciones`: timeout de notificación más generoso.
- `api-asistente`: catálogo de planes en el contexto de LicitAsist.

## Próximos pasos

8. Checklist final de seguridad (sección 6 del prompt) sobre todo lo
   construido.
9. `README.md` raíz con instrucciones de uso completas.
10. Actualizar `start-all.ps1` para incluir el frontend (`npm run dev`).
8. Scripts `start-all.ps1` / `stop-all.ps1`, checklist final de seguridad,
   `README.md`.
