# Checklist: Modelo ER vs. base de datos implementada

Generado por `scripts/dev/checklist_er.py` consultando `information_schema` de PostgreSQL (estructura real creada por Flyway).
REF = referencia a otro microservicio: no es FK (se valida por REST).

**Resultado: 124 de 124 verificaciones correctas.**

## usuarios_bd

Tablas: ER 9 · BD 9 · **idénticas**

| Tabla | Columna | Tipo ER | Tipo PostgreSQL | Nulo ER | Nulo BD | Clave ER | Clave BD | ✔ |
|---|---|---|---|---|---|---|---|---|
| rol | id | int | integer | no | no | PK | PK | ✅ |
| rol | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| usuario | id | int | integer | no | no | PK | PK | ✅ |
| usuario | email | texto | character varying | no | no | UQ | UQ | ✅ |
| usuario | password | bcrypt | character varying | no | no | — | — | ✅ |
| usuario | rol_id | int | integer | no | no | FK | FK | ✅ |
| usuario | activo | bool | boolean | no | no | — | — | ✅ |
| usuario | created_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| administrador | id | int | integer | no | no | PK | PK | ✅ |
| administrador | usuario_id | int | integer | no | no | FK,UQ | FK,UQ | ✅ |
| administrador | nombre | texto | character varying | no | no | — | — | ✅ |
| administrador | area | texto | character varying | no | no | — | — | ✅ |
| licitador | id | int | integer | no | no | PK | PK | ✅ |
| licitador | usuario_id | int | integer | no | no | FK,UQ | FK,UQ | ✅ |
| licitador | razon_social | texto | character varying | no | no | — | — | ✅ |
| licitador | rut | texto | character varying | no | no | UQ | UQ | ✅ |
| licitador | nombre_contacto | texto | character varying | no | no | — | — | ✅ |
| licitador | email_contacto | texto | character varying | no | no | — | — | ✅ |
| licitador | telefono | texto | character varying | no | no | — | — | ✅ |
| licitador | rubro_id | int | integer | no | no | FK | FK | ✅ |
| licitador | ciudad_id | int | integer | no | no | FK | FK | ✅ |
| licitador | descripcion_empresa | texto | text | no | no | — | — | ✅ |
| licitador | sitio_web | texto? | character varying | sí | sí | — | — | ✅ |
| licitador | updated_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| pyme | id | int | integer | no | no | PK | PK | ✅ |
| pyme | usuario_id | int | integer | no | no | FK,UQ | FK,UQ | ✅ |
| pyme | razon_social | texto | character varying | no | no | — | — | ✅ |
| pyme | rut | texto | character varying | no | no | UQ | UQ | ✅ |
| pyme | nombre_contacto | texto | character varying | no | no | — | — | ✅ |
| pyme | email_contacto | texto | character varying | no | no | — | — | ✅ |
| pyme | telefono | texto | character varying | no | no | — | — | ✅ |
| pyme | rubro_id | int | integer | no | no | FK | FK | ✅ |
| pyme | ciudad_id | int | integer | no | no | FK | FK | ✅ |
| pyme | tamano_empresa_id | int | integer | no | no | FK | FK | ✅ |
| pyme | descripcion_empresa | texto | text | no | no | — | — | ✅ |
| pyme | sitio_web | texto? | character varying | sí | sí | — | — | ✅ |
| pyme | updated_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| rubro | id | int | integer | no | no | PK | PK | ✅ |
| rubro | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| region | id | int | integer | no | no | PK | PK | ✅ |
| region | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| ciudad | id | int | integer | no | no | PK | PK | ✅ |
| ciudad | nombre | texto | character varying | no | no | — | — | ✅ |
| ciudad | region_id | int | integer | no | no | FK | FK | ✅ |
| tamano_empresa | id | int | integer | no | no | PK | PK | ✅ |
| tamano_empresa | nombre | texto | character varying | no | no | UQ | UQ | ✅ |

## licitaciones_bd

Tablas: ER 5 · BD 5 · **idénticas**

| Tabla | Columna | Tipo ER | Tipo PostgreSQL | Nulo ER | Nulo BD | Clave ER | Clave BD | ✔ |
|---|---|---|---|---|---|---|---|---|
| licitacion | id | int | integer | no | no | PK | PK | ✅ |
| licitacion | licitador_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| licitacion | titulo | texto | character varying | no | no | — | — | ✅ |
| licitacion | descripcion | texto | text | no | no | — | — | ✅ |
| licitacion | rubro_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| licitacion | region_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| licitacion | presupuesto_min | numeric? | numeric | sí | sí | — | — | ✅ |
| licitacion | presupuesto_max | numeric? | numeric | sí | sí | — | — | ✅ |
| licitacion | max_postulantes | int? | integer | sí | sí | — | — | ✅ |
| licitacion | imagen_url | texto? | character varying | sí | sí | — | — | ✅ |
| licitacion | archivo_url | texto? | character varying | sí | sí | — | — | ✅ |
| licitacion | archivo_nombre | texto? | character varying | sí | sí | — | — | ✅ |
| licitacion | tipo_archivo_id | int? | integer | sí | sí | FK | FK | ✅ |
| licitacion | fecha_cierre | date | date | no | no | — | — | ✅ |
| licitacion | estado_licitacion_id | int | integer | no | no | FK | FK | ✅ |
| licitacion | created_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| postulacion | id | int | integer | no | no | PK | PK | ✅ |
| postulacion | licitacion_id | int | integer | no | no | FK | FK | ✅ |
| postulacion | pyme_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| postulacion | mensaje | texto? | text | sí | sí | — | — | ✅ |
| postulacion | fecha_postulacion | date | date | no | no | — | — | ✅ |
| postulacion | estado_postulacion_id | int | integer | no | no | FK | FK | ✅ |
| postulacion | updated_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| tipo_archivo | id | int | integer | no | no | PK | PK | ✅ |
| tipo_archivo | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| estado_licitacion | id | int | integer | no | no | PK | PK | ✅ |
| estado_licitacion | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| estado_postulacion | id | int | integer | no | no | PK | PK | ✅ |
| estado_postulacion | nombre | texto | character varying | no | no | UQ | UQ | ✅ |

## ventas_bd

Tablas: ER 7 · BD 7 · **idénticas**

| Tabla | Columna | Tipo ER | Tipo PostgreSQL | Nulo ER | Nulo BD | Clave ER | Clave BD | ✔ |
|---|---|---|---|---|---|---|---|---|
| plan_suscripcion | id | int | integer | no | no | PK | PK | ✅ |
| plan_suscripcion | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| suscripcion | id | int | integer | no | no | PK | PK | ✅ |
| suscripcion | usuario_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| suscripcion | plan_id | int | integer | no | no | FK | FK | ✅ |
| suscripcion | estado_suscripcion_id | int | integer | no | no | FK | FK | ✅ |
| venta | id | int | integer | no | no | PK | PK | ✅ |
| venta | suscripcion_id | int | integer | no | no | FK | FK | ✅ |
| venta | monto | numeric | numeric | no | no | — | — | ✅ |
| venta | fecha | date | date | no | no | — | — | ✅ |
| pago | id | int | integer | no | no | PK | PK | ✅ |
| pago | venta_id | int | integer | no | no | FK,UQ | FK,UQ | ✅ |
| pago | id_transaccion | texto | character varying | no | no | — | — | ✅ |
| pago | metodo_pago_id | int | integer | no | no | FK | FK | ✅ |
| pago | estado_pago_id | int | integer | no | no | FK | FK | ✅ |
| estado_suscripcion | id | int | integer | no | no | PK | PK | ✅ |
| estado_suscripcion | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| estado_pago | id | int | integer | no | no | PK | PK | ✅ |
| estado_pago | nombre | texto | character varying | no | no | UQ | UQ | ✅ |
| metodo_pago | id | int | integer | no | no | PK | PK | ✅ |
| metodo_pago | nombre | texto | character varying | no | no | UQ | UQ | ✅ |

## notificaciones_bd

Tablas: ER 2 · BD 2 · **idénticas**

| Tabla | Columna | Tipo ER | Tipo PostgreSQL | Nulo ER | Nulo BD | Clave ER | Clave BD | ✔ |
|---|---|---|---|---|---|---|---|---|
| notificacion | id | int | integer | no | no | PK | PK | ✅ |
| notificacion | usuario_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| notificacion | tipo_notificacion_id | int | integer | no | no | FK | FK | ✅ |
| notificacion | canal | texto | character varying | no | no | — | — | ✅ |
| notificacion | created_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| tipo_notificacion | id | int | integer | no | no | PK | PK | ✅ |
| tipo_notificacion | nombre | texto | character varying | no | no | UQ | UQ | ✅ |

## chat_bd

Tablas: ER 2 · BD 2 · **idénticas**

| Tabla | Columna | Tipo ER | Tipo PostgreSQL | Nulo ER | Nulo BD | Clave ER | Clave BD | ✔ |
|---|---|---|---|---|---|---|---|---|
| conversacion | id | int | integer | no | no | PK | PK | ✅ |
| conversacion | postulacion_id | int | integer | no | no | REF,UQ | UQ | ✅ |
| conversacion | licitador_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| conversacion | pyme_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| conversacion | created_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| mensaje | id | int | integer | no | no | PK | PK | ✅ |
| mensaje | conversacion_id | int | integer | no | no | FK | FK | ✅ |
| mensaje | emisor_id | int | integer | no | no | REF | sin FK (REF) | ✅ |
| mensaje | contenido | texto | text | no | no | — | — | ✅ |
| mensaje | enviado_at | timestamp | timestamp without time zone | no | no | — | — | ✅ |
| mensaje | leido | bool | boolean | no | no | — | — | ✅ |

## Valores de catálogo (seed Flyway)

| Base | Tabla | Valores ER | Valores BD | ✔ |
|---|---|---|---|---|
| usuarios_bd | rol | Licitador, Pyme, Administrador | Licitador, Pyme, Administrador | ✅ |
| usuarios_bd | tamano_empresa | Pequeña, Mediana, Grande | Pequeña, Mediana, Grande | ✅ |
| licitaciones_bd | estado_licitacion | Abierta, Cerrada, Adjudicada | Abierta, Cerrada, Adjudicada | ✅ |
| licitaciones_bd | estado_postulacion | Pendiente, Aprobada, Rechazada | Pendiente, Aprobada, Rechazada | ✅ |
| licitaciones_bd | tipo_archivo | PDF, DOCX, XLSX, Otro | PDF, DOCX, XLSX, Otro | ✅ |
| ventas_bd | plan_suscripcion | Estándar, Premium | Estándar, Premium | ✅ |
| ventas_bd | estado_suscripcion | Activa, Vencida, Cancelada | Activa, Vencida, Cancelada | ✅ |
| ventas_bd | estado_pago | Aprobado, Rechazado, Pendiente | Aprobado, Rechazado, Pendiente | ✅ |
| ventas_bd | metodo_pago | Crédito, Débito | Crédito, Débito | ✅ |
| notificaciones_bd | tipo_notificacion | Licitación publicada, Postulación recibida, Postulación aprobada, Postulación rechazada, Pago confirmado, Mensaje nuevo | Licitación publicada, Postulación recibida, Postulación aprobada, Postulación rechazada, Pago confirmado, Mensaje nuevo | ✅ |
| usuarios_bd | region / ciudad | Regiones y ciudades de Chile (decisión del cliente) | 16 regiones, 346 comunas | ✅ |
| usuarios_bd | rubro | Ejemplos del ER: Construcción, Tecnología, Salud | Construcción, Tecnología, Salud, Rubro 1007182357 | ✅ |
