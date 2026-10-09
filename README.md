# LicitaWatch v2

Plataforma privada de gestión de licitaciones (Proyecto de título 2026).

- Los **Licitadores** publican y administran licitaciones sin costo.
- Las **Pymes** buscan, filtran y postulan con el plan **Estándar** (gratis) o **Premium** (pagado con Webpay Plus).
- Al adjudicarse una licitación, ambas partes conversan por un **chat privado**.
- Todos reciben **avisos por correo** y cuentan con **LicitAsist**, un asistente con IA (Groq) que responde con datos reales de la plataforma.
- El **Administrador** gestiona usuarios y roles, modera licitaciones y revisa ventas y suscripciones.

La ejecución es **100 % local (on-premise), sin Docker**.

---

## Índice

1. [Tecnologías](#tecnologías)
2. [Requisitos](#requisitos)
3. [Arquitectura](#arquitectura)
4. [Estructura del repositorio](#estructura-del-repositorio)
5. [Ejecución local paso a paso](#ejecución-local-paso-a-paso)
6. [Variables de entorno](#variables-de-entorno)
7. [Reglas de negocio implementadas](#reglas-de-negocio-implementadas)
8. [Pruebas](#pruebas)
9. [Webpay Plus (sandbox)](#webpay-plus-sandbox)
10. [Solución de problemas](#solución-de-problemas)

---

## Tecnologías

### Backend

| Tecnología | Versión | Uso |
|---|---|---|
| Java (JDK) | 21 LTS | Lenguaje de todos los microservicios |
| Maven | 3.9+ | Build multimódulo (POM padre en la raíz) |
| Spring Boot | 3.3.13 | Base de los 19 microservicios + gateway |
| Spring Framework | 6.1.x | Web MVC, `@HttpExchange` (clientes REST declarativos) |
| Spring Cloud Gateway | 2023.0.6 (Gateway 4.1.x) | `api-gateway`: enrutamiento, filtros de seguridad |
| Spring Security + OAuth2 Resource Server | 6.3.x | Validación de JWT y RBAC en el gateway |
| JJWT | 0.12.6 | Emisión de JWT HS512 (`MS.usuarios.bs`) |
| Spring Data JPA / Hibernate | 2024.0.x / 6.5.3 | Acceso a datos en las capas `bd` |
| Flyway | 10.10.0 | Migraciones y datos semilla de cada base |
| Driver JDBC PostgreSQL | 42.7.x | Conexión a las bases |
| springdoc-openapi | 2.6.0 | Swagger UI por microservicio |
| Bucket4j + Caffeine | 8.10.1 / 3.1.8 | Rate limiting en el gateway |
| Transbank SDK Java | 6.2.1 | Webpay Plus (`MS.ventas.ambassador`) |
| Spring Mail (Jakarta Mail) + Thymeleaf | 2.1.x / 3.1.3 | Correos HTML vía Gmail SMTP |
| Lombok | 1.18.38 | Reducción de código repetitivo |
| JUnit 5 + Mockito | 5.10.x | Pruebas unitarias y de controladores |

### Frontend

| Tecnología | Versión | Uso |
|---|---|---|
| Node.js / npm | 20+ | Entorno de build |
| React / React DOM | 18.3 | Interfaz de usuario (SPA) |
| TypeScript | 5.6 | Tipado estático |
| Vite | 5.4 | Servidor de desarrollo y build |
| Tailwind CSS (+ PostCSS, Autoprefixer) | 3.4 | Estilos |
| React Router DOM | 6.x | Navegación |
| Axios | 1.x | Cliente HTTP hacia el gateway |
| Zustand | 4.5 | Estado global (sesión) |
| React Markdown + remark-gfm | 9.x / 4.x | Respuestas de LicitAsist |
| Lucide React | 0.453 | Íconos |

### Infraestructura y servicios externos

| Tecnología | Versión / detalle | Uso |
|---|---|---|
| PostgreSQL | 16+ (probado en 18) | 5 bases, una por dominio |
| Webpay Plus (Transbank) | Ambiente de integración (sandbox) | Pago del plan Premium |
| Groq API | Modelo `openai/gpt-oss-120b` (configurable) | LLM de LicitAsist |
| Gmail SMTP | Contraseña de aplicación | Envío de correos |
| Python 3 | 3.10+ (opcional) | Scripts de prueba E2E y checklist del modelo ER |

---

## Requisitos

Para ejecutar el proyecto en un equipo local (probado en **Windows 10/11**):

| Requisito | Detalle |
|---|---|
| **JDK 21** | `java -version` debe mostrar 21. Configura `JAVA_HOME`. |
| **Maven 3.9+** | `mvn -v` |
| **Node.js 20+ y npm** | `node -v`, `npm -v` |
| **PostgreSQL 16+** | Escuchando en `localhost:5432`, con un usuario con permiso para crear bases (por ejemplo `postgres`). `psql` debe estar en el PATH, o en `C:\Program Files\PostgreSQL\<versión>\bin`. |
| **Cuenta Gmail** | Con verificación en dos pasos y una *contraseña de aplicación* para SMTP. |
| **API key de Groq** | Se obtiene gratis en <https://console.groq.com>. |
| **PowerShell 5.1+** | Para los scripts de arranque (incluido en Windows). |
| Python 3 (opcional) | Solo para `scripts/dev/e2e.py` y `scripts/dev/checklist_er.py`. |

Puertos que deben estar libres: **8080**, **5173**, **8111–8116**, **8211–8216**, **8311–8316** y **8413**.

---

## Arquitectura

El sistema es una arquitectura de **microservicios por dominio**, con cuatro capas: **BFF → BS → BD** y un **ambassador** para la pasarela de pago. Hay seis dominios: usuarios, licitaciones, ventas, notificaciones, asistente y chat. En total son **19 microservicios Spring Boot** más el **api-gateway** y el **frontend React**.

```
                     ┌──────────────────────────────────────────┐
  Navegador ───────▶ │ licitawatch-frontend (React + TS, :5173) │
                     └──────────────────────┬───────────────────┘
                                            │ HTTP + JWT
                     ┌──────────────────────▼────────────────────────────────────────────┐
                     │ api-gateway (:8080)                                                │
                     │ JWT · RBAC · CORS · rate limit · filtro SQLi · X-Internal-Key      │
                     └──────────────────────┬────────────────────────────────────────────┘
          ┌───────────────┬───────────────┬─┴────────────┬────────────────┬──────────────┐
   MS.bff.usuarios  MS.bff.licitaciones  MS.bff.ventas  MS.bff.notificaciones  MS.bff.asistente  MS.bff.chat
       (:8111)          (:8112)            (:8113)          (:8114)              (:8115)          (:8116)
          │               │               │                │                    │               │
   MS.usuarios.bs  MS.licitaciones.bs  MS.ventas.bs ───▶ MS.ventas.ambassador ───▶ Webpay Plus (sandbox)
       (:8211)          (:8212)          (:8213)            (:8413)
          │               │               │        MS.notificaciones.bs ──▶ Gmail SMTP
          │               │               │             (:8214)
          │               │               │                │      MS.asistente.bs ──▶ Groq (LLM)
          │               │               │                │          (:8215)        MS.chat.bs (:8216)
          │               │               │                │                              │
   MS.usuarios.bd  MS.licitaciones.bd  MS.ventas.bd  MS.notificaciones.bd              MS.chat.bd
       (:8311)          (:8312)          (:8313)          (:8314)                       (:8316)
          │               │               │                │                              │
     usuarios_bd    licitaciones_bd     ventas_bd    notificaciones_bd                     chat_bd
```

### Responsabilidad de cada capa

| Capa | Responsabilidad |
|---|---|
| **Frontend** | SPA en React. Habla **solo** con el api-gateway (`VITE_API_BASE_URL`). |
| **api-gateway** | Único punto de entrada. Valida el JWT, aplica RBAC por rol (`LICITADOR`, `PYME`, `ADMINISTRADOR`), CORS, rate limiting (Bucket4j) y bloqueo de patrones de inyección SQL. Reenvía la identidad del usuario en cabeceras `X-User-*` (descarta las que mande el cliente) y agrega la clave interna `X-Internal-Key`. |
| **MS.bff.\<dominio\>** (Backend For Frontend) | Adapta la API a lo que necesita el frontend. No tiene reglas de negocio. |
| **MS.\<dominio\>.bs** (Business Service) | Contiene **todas las reglas de negocio**. Los BS se comunican entre sí por REST. Los campos de referencia a otro dominio se validan llamando al microservicio dueño del dato. |
| **MS.\<dominio\>.bd** (Data Service) | Solo acceso a datos: Spring Data JPA + Flyway (`ddl-auto=validate`). Cada dominio tiene **su propia base PostgreSQL** y no hay claves foráneas entre bases. |
| **MS.ventas.ambassador** | Aísla la pasarela de pago detrás de la interfaz `PasarelaPagoService`. El resto del sistema no conoce a Transbank. |
| **licitawatch-common** | Librería compartida: errores uniformes (`@RestControllerAdvice`), excepciones propias, contexto del usuario autenticado y clientes REST declarativos (`@HttpExchange`). |

### Seguridad

- **Autenticación:** `MS.usuarios.bs` emite un **JWT HS512** con `sub=usuarioId`, `email`, `rol`, `perfilId` y `nombre`. El gateway lo valida en cada solicitud.
- **Tokens de correo:** la confirmación de cuenta y la recuperación de contraseña usan claves **derivadas** del secreto, así que el gateway no los acepta como sesión.
- **Tráfico interno:** los microservicios escuchan en `127.0.0.1` y rechazan cualquier solicitud que no traiga `X-Internal-Key`. Solo el gateway queda expuesto.
- **Contraseñas:** se guardan con hash BCrypt.

### Bases de datos

| Base | Microservicio dueño | Contenido principal |
|---|---|---|
| `usuarios_bd` | `MS.usuarios.bd` | usuario, pyme, licitador, administrador, rol, región, ciudad, rubro, tamaño de empresa |
| `licitaciones_bd` | `MS.licitaciones.bd` | licitación, postulación y catálogos de estado |
| `ventas_bd` | `MS.ventas.bd` | suscripción, venta, pago y catálogos |
| `notificaciones_bd` | `MS.notificaciones.bd` | notificación, tipo de notificación |
| `chat_bd` | `MS.chat.bd` | conversación, mensaje |

Las tablas y los datos semilla (catálogos, regiones y ciudades) los crea **Flyway** automáticamente al levantar cada `MS.<dominio>.bd`. Las migraciones están en `*/ms-*-bd/src/main/resources/db/migration/`.

### Puertos

| Dominio | BFF | BS | BD | Base PostgreSQL |
|---|---|---|---|---|
| Usuarios | 8111 | 8211 | 8311 | `usuarios_bd` |
| Licitaciones | 8112 | 8212 | 8312 | `licitaciones_bd` |
| Ventas | 8113 | 8213 | 8313 | `ventas_bd` (+ ambassador **8413**) |
| Notificaciones | 8114 | 8214 | 8314 | `notificaciones_bd` |
| Asistente (LicitAsist) | 8115 | 8215 | — | sin base de datos |
| Chat | 8116 | 8216 | 8316 | `chat_bd` |
| **api-gateway** | **8080** | | | |
| **Frontend** | **5173** | | | |

Cada microservicio tiene su Swagger en `http://localhost:<puerto>/swagger-ui.html` y su health check en `/actuator/health`.

---

## Estructura del repositorio

```
LicitaWatch/
├── pom.xml                    POM padre y agregador (Spring Boot 3.3, Java 21)
├── .env.example               Plantilla de variables de entorno (sin secretos)
├── licitawatch-common/        Librería compartida
├── api-gateway/               Spring Cloud Gateway
├── usuarios/                  ms-usuarios-bff · ms-usuarios-bs · ms-usuarios-bd
├── licitaciones/              ms-licitaciones-bff · ms-licitaciones-bs · ms-licitaciones-bd
├── ventas/                    ms-ventas-bff · ms-ventas-bs · ms-ventas-bd · ms-ventas-ambassador
├── notificaciones/            ms-notificaciones-bff · ms-notificaciones-bs · ms-notificaciones-bd
├── asistente/                 ms-asistente-bff · ms-asistente-bs
├── chat/                      ms-chat-bff · ms-chat-bs · ms-chat-bd
├── frontend/                  React + TypeScript + Vite + Tailwind
├── db/crear-bases.sql         Crea las 5 bases PostgreSQL
├── scripts/
│   ├── crear-bases.ps1        Crea las bases usando las credenciales del .env
│   ├── start-all.ps1          Compila (si hace falta) y levanta todo en orden
│   ├── stop-all.ps1           Detiene todos los servicios
│   └── dev/                   e2e.py, checklist_er.py y utilidades de desarrollo
└── docs/                      Checklist del modelo ER, modelo de datos y evidencias
```

---

## Ejecución local paso a paso

> Todos los comandos se ejecutan en **PowerShell**, desde la raíz del proyecto.

### 1. Clonar el repositorio

```powershell
git clone https://github.com/PabloMaximilianoBG/LicitawatchTest.git
cd LicitawatchTest
```

### 2. Configurar las variables de entorno

```powershell
Copy-Item .env.example .env
```

Abre `.env` y completa todos los valores `<completar>`:

| Variable | Qué poner |
|---|---|
| `POSTGRES_PASSWORD` | Contraseña de tu usuario de PostgreSQL |
| `JWT_SECRET` | Cadena aleatoria de **al menos 64 caracteres** |
| `INTERNAL_API_KEY` | Cadena aleatoria (clave del tráfico interno) |
| `GROQ_API_KEY` | Tu API key de Groq |
| `SMTP_USERNAME`, `SMTP_FROM` | Tu correo Gmail |
| `SMTP_PASSWORD` | La *contraseña de aplicación* de Gmail (no la contraseña normal) |
| `SOPORTE_EMAIL` | Casilla que recibirá las solicitudes de soporte |
| `ADMIN_PASSWORD` | Contraseña del administrador inicial |

Para generar secretos aleatorios en PowerShell:

```powershell
-join ((48..57)+(65..90)+(97..122) | Get-Random -Count 64 | ForEach-Object {[char]$_})
```

Las credenciales de Webpay que trae la plantilla son las **públicas del ambiente de integración** de Transbank y no hay que cambiarlas.

> `.env` contiene secretos y está en `.gitignore`. **Nunca lo subas al repositorio.**

### 3. Crear las bases de datos

```powershell
powershell -ExecutionPolicy Bypass -File scripts\crear-bases.ps1
```

O, si prefieres hacerlo con `psql` directamente:

```powershell
psql -U postgres -h localhost -f db\crear-bases.sql
```

Solo crea las 5 bases vacías. Flyway crea las tablas en el paso 5.

### 4. Compilar el backend

```powershell
mvn -DskipTests install
```

Instala el POM padre y `licitawatch-common` en el repositorio local de Maven y genera los `.jar` de los 20 módulos. La primera vez tarda algunos minutos porque descarga las dependencias.

### 5. Levantar todo el sistema

```powershell
powershell -ExecutionPolicy Bypass -File scripts\start-all.ps1
```

El script levanta los servicios **por capas**, en paralelo dentro de cada capa, y espera a que cada uno responda `/actuator/health` antes de pasar a la siguiente:

1. `MS.*.bd` + `MS.ventas.ambassador` (aquí Flyway crea las tablas)
2. `MS.*.bs`
3. `MS.bff.*`
4. `api-gateway`
5. Frontend (ejecuta `npm install` si hace falta y luego `npm run dev`)

Opciones:

```powershell
scripts\start-all.ps1 -Build         # fuerza recompilación (mvn install)
scripts\start-all.ps1 -SinFrontend   # solo backend
```

Los logs quedan en la carpeta `.run\` (un archivo por servicio).

### 6. Abrir la aplicación

- **Aplicación web:** <http://localhost:5173>
- **API (gateway):** <http://localhost:8080>

Al arrancar, `MS.usuarios.bs` crea el **administrador inicial** con `ADMIN_EMAIL` / `ADMIN_PASSWORD` del `.env` (por defecto `admin@licitawatch.local`). Desde la página de registro puedes crear cuentas de Licitador y de Pyme.

### 7. Detener el sistema

```powershell
powershell -ExecutionPolicy Bypass -File scripts\stop-all.ps1
```

### Ejecución manual (alternativa)

Cada servicio se puede levantar por separado desde su carpeta (perfil `local`; los puertos y URLs están en su `application.yml`):

```powershell
cd usuarios\ms-usuarios-bd
mvn spring-boot:run
```

Respeta el orden **bd → bs → bff → gateway**. El frontend se levanta aparte:

```powershell
cd frontend
npm install
npm run dev
```

---

## Variables de entorno

Las lee cada microservicio al arrancar (`spring.config.import` del `.env` de la raíz). El frontend solo lee las que empiezan con `VITE_`.

| Variable | Uso |
|---|---|
| `POSTGRES_HOST/PORT/USER/PASSWORD`, `DB_*_NAME` | Conexión de cada `MS.*.bd` a su base |
| `JWT_SECRET`, `JWT_EXPIRATION` | Firma del JWT (`MS.usuarios.bs`) y validación (api-gateway) |
| `INTERNAL_API_KEY` | Clave del tráfico interno gateway → bff → bs → bd |
| `WEBPAY_COMMERCE_CODE`, `WEBPAY_API_KEY`, `WEBPAY_ENVIRONMENT=SANDBOX`, `WEBPAY_RETURN_URL` | Webpay Plus (ambassador) |
| `PREMIUM_PRECIO`, `PREMIUM_VIGENCIA_DIAS`, `ESTANDAR_POSTULACIONES_MES`, `PREMIUM_POSTULACIONES_MES` | Condiciones de los planes |
| `SMTP_HOST/PORT/USERNAME/PASSWORD/FROM`, `SOPORTE_EMAIL` | Gmail SMTP y casilla de soporte |
| `GROQ_API_KEY`, `GROQ_MODEL`, `GROQ_API_BASE_URL` | LicitAsist |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `ADMIN_NAME`, `ADMIN_AREA` | Administrador inicial |
| `STORAGE_DIR`, `PUBLIC_BASE_URL` | Carpeta de imágenes y documentos de licitaciones, y base de sus URL |
| `RATE_LIMIT_*_PER_MINUTE` | Límites de solicitudes del gateway |
| `*_BFF_URL`, `*_BS_URL`, `*_BD_URL`, `VENTAS_AMBASSADOR_URL`, `FRONTEND_URL` | URLs entre servicios (perfil local) |
| `VITE_API_BASE_URL` | URL del gateway para el frontend |

---

## Reglas de negocio implementadas

| Regla | Dónde |
|---|---|
| Solo las Pymes pagan; el Licitador publica sin costo | `MS.ventas.bs` (403 a Licitador) + RBAC del gateway |
| Plan Estándar gratis por defecto: búsqueda sin límite, 3 postulaciones/mes, chat al adjudicar, soporte estándar | `MS.ventas.bs` (plan), `MS.licitaciones.bs` (límite mensual) |
| Plan Premium: 7 postulaciones/mes, LicitAsist, prioridad de visibilidad, alertas por correo del rubro, insignia, soporte prioritario | `MS.licitaciones.bs`, `MS.asistente.bs`, `MS.notificaciones.bs`, `MS.usuarios.bs` |
| Pago Premium con Webpay Plus sandbox vía ambassador; mientras no se paga, la venta se muestra "Pendiente" | `MS.ventas.bs` + `MS.ventas.ambassador` |
| Vigencia Premium = fecha de la venta pagada + `PREMIUM_VIGENCIA_DIAS`; al vencer, vuelve a Estándar | `MS.ventas.bs` (job diario + verificación al consultar) |
| Licitador: publicar, editar, cerrar, eliminar y ver postulantes; adjuntar imagen y documento | `MS.licitaciones.bs` |
| Cierre automático al pasar la fecha de cierre; no se postula a una licitación cerrada ni sin cupos | `MS.licitaciones.bs` |
| La postulación nace Pendiente; al aprobar una, la licitación queda Adjudicada y las demás se rechazan y notifican | `MS.licitaciones.bd` (una transacción) + `.bs` |
| Chat disponible al aprobar una postulación; solo las dos partes leen y escriben | `MS.chat.bs` |
| Correos: licitación publicada, postulación recibida, aprobada/rechazada, pago confirmado, mensaje nuevo | `MS.notificaciones.bs` |
| LicitAsist: libre para Licitador y Administrador; la Pyme solo con Premium | `MS.asistente.bs` |
| Administrador: crear usuarios de cualquier rol, editar, activar/desactivar, cambiar rol, moderar licitaciones, ver ventas/suscripciones/notificaciones, agregar rubros | BS de cada dominio |
| Registro con confirmación de cuenta por correo y recuperación de contraseña | `MS.usuarios.bs` |

---

## Pruebas

| Tipo | Comando |
|---|---|
| Unitarias y de controladores (JUnit 5 + Mockito + `@WebMvcTest`), incluida la prueba de seguridad del gateway | `mvn test` (todo) o `mvn -f <modulo>/pom.xml test` |
| End-to-end contra el sistema levantado (solo a través del gateway) | `python scripts/dev/e2e.py` |
| Comparación del modelo ER con la base real | `python scripts/dev/checklist_er.py` → genera `docs/checklist-modelo-er.md` |

---

## Webpay Plus (sandbox)

Es el ambiente de integración de Transbank, así que no hay cobros reales. Datos de prueba:

| Dato | Valor |
|---|---|
| VISA (aprobada) | `4051 8856 0044 6623` |
| Mastercard (rechazada) | `5186 0595 5959 0568` |
| CVV | `123` |
| RUT / clave (autenticación del banco) | `11.111.111-1` / `123` |

---

## Solución de problemas

| Problema | Solución |
|---|---|
| `Falta el archivo .env en la raiz` | Copia `.env.example` como `.env` y complétalo (paso 2). |
| Un servicio no levanta | Revisa su log en `.run\<servicio>.log`. |
| Error de la JVM con sockets / `unixdomain` en Windows cuando la ruta del usuario tiene espacios | El proyecto ya usa `C:\licitawatch-tmp` como directorio temporal (configurado en el `pom.xml` y en `start-all.ps1`). Verifica que la carpeta se pueda crear. |
| `password authentication failed` | Revisa `POSTGRES_USER` / `POSTGRES_PASSWORD` en `.env`. |
| Flyway: `Validate failed` | La base tiene un esquema distinto. Bórrala y vuelve a crearla (paso 3). |
| Puerto ocupado | Ejecuta `scripts\stop-all.ps1` o libera el puerto indicado en la tabla de puertos. |
| No llegan correos | Usa una *contraseña de aplicación* de Gmail en `SMTP_PASSWORD` y revisa la carpeta de spam. |
| LicitAsist responde con error | Revisa `GROQ_API_KEY` y que `GROQ_MODEL` exista en tu cuenta de Groq. |
