CREATE TABLE plan_suscripcion (
    id                          BIGSERIAL PRIMARY KEY,
    nombre                      VARCHAR(20) NOT NULL UNIQUE CHECK (nombre IN ('ESTANDAR', 'PREMIUM')),
    descripcion                 VARCHAR(300) NOT NULL,
    precio                      NUMERIC(12, 2) NOT NULL,
    limite_publicaciones_mes    INT,
    limite_postulaciones_mes    INT,
    soporte_prioritario         BOOLEAN NOT NULL DEFAULT FALSE,
    notificaciones_automaticas  BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE suscripcion (
    id                  BIGSERIAL PRIMARY KEY,
    usuario_id          BIGINT NOT NULL,
    plan_id             BIGINT NOT NULL REFERENCES plan_suscripcion (id),
    estado              VARCHAR(10) NOT NULL CHECK (estado IN ('ACTIVA', 'VENCIDA')),
    fecha_inicio        DATE,
    fecha_vencimiento   DATE,
    creado_en           TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_suscripcion_usuario ON suscripcion (usuario_id);

CREATE TABLE venta (
    id               BIGSERIAL PRIMARY KEY,
    suscripcion_id   BIGINT NOT NULL REFERENCES suscripcion (id),
    monto            NUMERIC(12, 2) NOT NULL,
    fecha            DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE pago (
    id              BIGSERIAL PRIMARY KEY,
    venta_id        BIGINT NOT NULL UNIQUE REFERENCES venta (id),
    token           VARCHAR(64) NOT NULL UNIQUE,
    id_transaccion  VARCHAR(64),
    estado          VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO'))
);
