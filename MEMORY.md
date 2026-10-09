# LICITAWATCH — MEMORIA DEL PROYECTO (v2)

> Leer SIEMPRE antes de modificar el proyecto y actualizar tras cada cambio.
> Estados: `[ ]` Pendiente · `[~]` En desarrollo · `[x]` Implementado · `[✓]` Implementado y probado · `[!]` Problema

## Fuentes de verdad
- **Estructura de datos = Modelo ER** (`Modelo_ER_LicitaWatch_.pdf`): 5 bases, tablas/columnas/catálogos SIN cambios. Prohibido agregar, quitar o renombrar tablas, columnas o valores de catálogo (ver `docs/checklist-modelo-er.md`).
- **Comportamiento = PPT** (`LicitaWatch_Presentacion.pdf`, 20 diapositivas). Lo no definido se pregunta al usuario (no se inventa).
- Decisiones del usuario del 2026-10-07 (respuestas a 16 preguntas): ver sección "Decisiones de negocio".

## Estado (2026-10-07)
- [✓] Refactor v2 completo: 19 microservicios (6 BFF, 6 BS, 5 BD, ambassador, api-gateway) + librería común + frontend nuevo.
- [✓] `mvn -DskipTests install` compila los 20 artefactos. Tests unitarios por módulo OK (JUnit 5 + Mockito en services, @WebMvcTest en controllers, gateway con BFF simulado).
- [✓] E2E real por el gateway (`python scripts/dev/e2e.py`): 76/76 OK (registro, confirmación, login, perfil, publicar con imagen/PDF, buscar, postular, límite plan, aprobar/adjudicar, chat, Webpay sandbox create + anulado, LicitAsist con Groq real, soporte, avisos, admin, seguridad). Correos reales vía Gmail.
- [✓] Checklist ER vs BD real (`python scripts/dev/checklist_er.py`): 124/124.
- [✓] Frontend: `tsc` y `vite build` OK; recorrido Playwright de 37 rutas × (1366 px y 375 px) sin desborde ni errores de consola.
- [ ] Pasos humanos: clic en correo real de confirmación / recuperación, pago aprobado en el formulario Webpay sandbox (tarjeta de prueba), LicitAsist como Pyme Premium real.
- [ ] Colección Postman v2: no existe (la v1 se eliminó por obsoleta; el reemplazo de verificación es `scripts/dev/e2e.py`).

## Arquitectura (PPT diap. 10-12)
frontend → api-gateway (8080) → MS.bff.<d> (81x) → MS.<d>.bs (82x) → MS.<d>.bd (83x) → <d>_bd. Ventas: + MS.ventas.ambassador (8413) → Webpay. Asistente sin BD (bs → Groq). Notificaciones bs → Gmail SMTP.
| Dominio | BFF | BS | BD | Base |
|---|---|---|---|---|
| usuarios | 8111 | 8211 | 8311 | usuarios_bd |
| licitaciones | 8112 | 8212 | 8312 | licitaciones_bd |
| ventas | 8113 | 8213 | 8313 | ventas_bd |
| notificaciones | 8114 | 8214 | 8314 | notificaciones_bd |
| asistente | 8115 | 8215 | — | — |
| chat | 8116 | 8216 | 8316 | chat_bd |

- Carpetas: `licitawatch-common/`, `usuarios/ms-usuarios-{bd,bs,bff}`, `licitaciones/…`, `ventas/…` (+`ms-ventas-ambassador`), `notificaciones/…`, `asistente/ms-asistente-{bs,bff}`, `chat/…`, `api-gateway/`, `frontend/`, `db/`, `scripts/`, `docs/`.
- POM raíz = padre (Boot 3.3.13, Spring Cloud 2023.0.6) + agregador. Paquetes `cl.licitawatch.<dominio>.<capa>`: controller, service, service.impl, repository, entity, dto.request/response, mapper, client(+dto), config.
- `licitawatch-common` (autoconfiguración): `GlobalExceptionHandler` + `ErrorResponse {timestamp,status,codigo,mensaje,ruta,errores}`, excepciones propias, `InternalAuthFilter` (exige `X-Internal-Key`, publica `UsuarioActual` desde `X-User-Id/Email/Rol`, `X-Perfil-Id`), `RestClientFactory` (interfaces `@HttpExchange`, propaga usuario y clave, errores remotos → `RemoteServiceException` con el mismo código).
- Gateway: Spring Security WebFlux + oauth2-resource-server (HS512, issuer `licitawatch`), RBAC por ruta, CORS (FRONTEND_URL), `PerimetroWebFilter` (SQLi 400, rate limit 429), `IdentidadGlobalFilter` (borra X-User-* del cliente y agrega los del JWT + clave interna).
- JWT: emitido por MS.usuarios.bs. Claims: sub=usuarioId, email, rol (LICITADOR|PYME|ADMINISTRADOR), perfilId, nombre. Tokens de correo con claves DERIVADAS (SHA-512(secret+":"+propósito)) → el gateway no los acepta como sesión.

## Decisiones de negocio (usuario, 2026-10-07)
1. Chat disponible al APROBAR; el Licitador hace clic en "Chatear" (crea `conversacion`).
2. Solo se aprueba una postulación: las demás Pendientes → Rechazada y se notifica "Postulación rechazada" (motivo: adjudicada a otra empresa).
3. `max_postulantes` alcanzado → la licitación deja de estar disponible (no aparece en la búsqueda y postular da 409 SIN_CUPOS).
4. Cierre automático (scheduler horario + al iniciar) cuando hoy > fecha_cierre; no se postula a Cerrada.
5. Precio y vigencia Premium por configuración (`PREMIUM_PRECIO=24990`, `PREMIUM_VIGENCIA_DIAS=30`); vigencia = venta.fecha del pago aprobado + días.
6. Estándar por defecto y gratis (sin venta). Se crea al registrar la Pyme y, si falta, al consultar el plan.
7. Prioridad de visibilidad = postulantes Premium primero.
8. `pago` se crea solo al volver de Webpay; antes: suscripcion Premium "Cancelada" + venta sin pago = "Pendiente" (se reutiliza al reintentar). Método: VD/VP→Débito, resto→Crédito.
9. LicitAsist libre para Licitador y Admin; Pyme solo Premium.
10/13. Admin: crear cuentas de cualquier rol, editar perfiles, activar/desactivar (bloqueo `!`+hash), cambiar rol/dar Administrador (se conserva el perfil anterior), moderar licitaciones (editar, cerrar/reabrir, eliminar), cancelar Premium, agregar rubros.
11. Eliminar licitación = borrado físico (postulaciones, archivos y conversaciones).
12. Imagen/documento se suben; URL generada `PUBLIC_BASE_URL/api/licitaciones/archivos/{id}/{nombre}` (STORAGE_DIR local).
14. 16 regiones y 346 comunas de Chile en `ciudad`. Rubros: los 3 ejemplos del ER (+ los que agregue el Admin).
15. Soporte funcional por correo a SOPORTE_EMAIL ([Soporte prioritario] para Premium).
16. Destinatarios de correo: publicada→Pymes Premium del rubro; recibida→Licitador; aprobada/rechazada→Pyme; pago→Pyme; mensaje nuevo→contraparte (solo si no tenía mensajes sin leer).
Extra: confirmar contraseña en registro y restablecer; recuperar contraseña por correo; confirmación de cuenta (usuario.activo=false hasta confirmar) — todo sin tablas nuevas.

## Ejecución local (Windows, sin Docker)
- `.env` en la raíz (PostgreSQL postgres/root local). `.env.example` sin secretos. Caracteres no ASCII en .env con escape `\u00f3`.
- Bases: `scripts\crear-bases.ps1` (o `db\crear-bases.sql`). Las bases v1 `licitawatch_*` siguen existiendo pero ya no se usan.
- Arranque: `scripts\start-all.ps1 [-Build] [-SinFrontend]` (por capas, ~4 min); `scripts\stop-all.ps1`. Logs `.run\`. JVM con `-Djdk.net.unixdomain.tmpdir=C:\licitawatch-tmp` (TEMP con espacios).
- Para recompilar un módulo en ejecución: detenerlo primero (el jar queda bloqueado en Windows).

## Bugs encontrados y solucionados (2026-10-07)
- `SimpleClientHttpRequestFactory` no soporta PATCH → `RestClientFactory` usa `JdkClientHttpRequestFactory`.
- Clientes `@HttpExchange` formateaban `LocalDate` como `10/7/26` → `@DateTimeFormat(iso = DATE)` en los parámetros.
- `MultipartBodyBuilder` requiere reactive-streams en servlet → multipart armado con `LinkedMultiValueMap`.
- Gmail SMTP "Read timed out" esporádico → timeouts 30 s + un reintento en `CorreoServiceImpl`.
- Gestión de licitación mostraba acciones a un Licitador no dueño → guardia en el frontend (el backend ya devolvía 403).
- Flyway 10.10 avisa "PostgreSQL 18.6 newer than supported" (funciona).

## Pendientes / mejoras futuras
- [ ] Colección Postman v2 si el usuario la pide.
- [ ] WebSocket para el chat (el ER define polling como MVP).
