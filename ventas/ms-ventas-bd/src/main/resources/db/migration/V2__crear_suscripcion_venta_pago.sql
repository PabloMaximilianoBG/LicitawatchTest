-- ============================================================
-- ventas-bd · Modelo ER (página 4): plan -> suscripción -> venta -> pago
-- usuario_id es REF a usuario.id de usuarios-bd (NO es FK): se valida por REST.
-- venta.monto guarda lo cobrado ese día (dato histórico).
-- ============================================================
CREATE TABLE suscripcion (
    id                    SERIAL  PRIMARY KEY,
    usuario_id            INTEGER NOT NULL,
    plan_id               INTEGER NOT NULL REFERENCES plan_suscripcion (id),
    estado_suscripcion_id INTEGER NOT NULL REFERENCES estado_suscripcion (id)
);

CREATE TABLE venta (
    id             SERIAL         PRIMARY KEY,
    suscripcion_id INTEGER        NOT NULL REFERENCES suscripcion (id),
    monto          NUMERIC(12, 2) NOT NULL,
    fecha          DATE           NOT NULL
);

CREATE TABLE pago (
    id             SERIAL       PRIMARY KEY,
    venta_id       INTEGER      NOT NULL UNIQUE REFERENCES venta (id),
    id_transaccion VARCHAR(100) NOT NULL,
    metodo_pago_id INTEGER      NOT NULL REFERENCES metodo_pago (id),
    estado_pago_id INTEGER      NOT NULL REFERENCES estado_pago (id)
);
