-- ============================================================
-- notificaciones-bd · Modelo ER (página 5): un registro por cada evento importante
-- usuario_id es REF a usuario.id de usuarios-bd (NO es FK). canal = 'email'.
-- ============================================================
CREATE TABLE notificacion (
    id                   SERIAL      PRIMARY KEY,
    usuario_id           INTEGER     NOT NULL,
    tipo_notificacion_id INTEGER     NOT NULL REFERENCES tipo_notificacion (id),
    canal                VARCHAR(20) NOT NULL,
    created_at           TIMESTAMP   NOT NULL
);
