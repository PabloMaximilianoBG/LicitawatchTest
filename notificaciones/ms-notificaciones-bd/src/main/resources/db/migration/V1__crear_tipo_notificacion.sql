-- ============================================================
-- notificaciones-bd · Modelo ER (página 5): catálogo de tipos de evento
-- ============================================================
CREATE TABLE tipo_notificacion (
    id     SERIAL      PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);
