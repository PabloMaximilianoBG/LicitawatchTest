-- ============================================================
-- chat-bd · Modelo ER (página 6): una conversación por postulación
-- postulacion_id, licitador_id, pyme_id y emisor_id son REF (NO son FK): se validan por REST.
-- ============================================================
CREATE TABLE conversacion (
    id             SERIAL    PRIMARY KEY,
    postulacion_id INTEGER   NOT NULL UNIQUE,
    licitador_id   INTEGER   NOT NULL,
    pyme_id        INTEGER   NOT NULL,
    created_at     TIMESTAMP NOT NULL
);

CREATE TABLE mensaje (
    id              SERIAL    PRIMARY KEY,
    conversacion_id INTEGER   NOT NULL REFERENCES conversacion (id),
    emisor_id       INTEGER   NOT NULL,
    contenido       TEXT      NOT NULL,
    enviado_at      TIMESTAMP NOT NULL,
    leido           BOOLEAN   NOT NULL
);
