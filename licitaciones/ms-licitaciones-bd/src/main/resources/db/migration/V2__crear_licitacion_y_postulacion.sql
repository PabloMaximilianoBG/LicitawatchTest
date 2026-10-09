-- ============================================================
-- licitaciones-bd · Modelo ER (página 3)
-- licitador_id, rubro_id, region_id y pyme_id son REF (NO son FK): se validan por REST.
-- Campos con '?' en el ER -> NULL; el resto NOT NULL.
-- ============================================================
CREATE TABLE licitacion (
    id                   SERIAL         PRIMARY KEY,
    licitador_id         INTEGER        NOT NULL,
    titulo               VARCHAR(200)   NOT NULL,
    descripcion          TEXT           NOT NULL,
    rubro_id             INTEGER        NOT NULL,
    region_id            INTEGER        NOT NULL,
    presupuesto_min      NUMERIC(15, 2),
    presupuesto_max      NUMERIC(15, 2),
    max_postulantes      INTEGER,
    imagen_url           VARCHAR(500),
    archivo_url          VARCHAR(500),
    archivo_nombre       VARCHAR(255),
    tipo_archivo_id      INTEGER        REFERENCES tipo_archivo (id),
    fecha_cierre         DATE           NOT NULL,
    estado_licitacion_id INTEGER        NOT NULL REFERENCES estado_licitacion (id),
    created_at           TIMESTAMP      NOT NULL
);

CREATE TABLE postulacion (
    id                    SERIAL    PRIMARY KEY,
    licitacion_id         INTEGER   NOT NULL REFERENCES licitacion (id),
    pyme_id               INTEGER   NOT NULL,
    mensaje               TEXT,
    fecha_postulacion     DATE      NOT NULL,
    estado_postulacion_id INTEGER   NOT NULL REFERENCES estado_postulacion (id),
    updated_at            TIMESTAMP NOT NULL
);
