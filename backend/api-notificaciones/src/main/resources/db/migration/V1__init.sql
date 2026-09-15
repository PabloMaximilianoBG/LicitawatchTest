CREATE TABLE notificacion (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT NOT NULL,
    tipo        VARCHAR(20) NOT NULL CHECK (tipo IN ('LICITACION', 'PAGO')),
    canal       VARCHAR(10) NOT NULL CHECK (canal IN ('EMAIL')),
    estado      VARCHAR(10) NOT NULL CHECK (estado IN ('ENVIADO', 'PENDIENTE')),
    fecha       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_notificacion_usuario ON notificacion (usuario_id);
