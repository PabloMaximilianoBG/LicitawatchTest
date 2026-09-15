CREATE TABLE licitacion (
    id              BIGSERIAL PRIMARY KEY,
    empresa_id      BIGINT NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    rubro           VARCHAR(100) NOT NULL,
    monto_estimado  NUMERIC(14, 2),
    region          VARCHAR(100) NOT NULL,
    estado          VARCHAR(20) NOT NULL CHECK (estado IN ('PUBLICADA', 'CERRADA')),
    fecha_cierre    DATE NOT NULL,
    creado_en       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_licitacion_empresa ON licitacion (empresa_id);
CREATE INDEX idx_licitacion_estado_rubro_region ON licitacion (estado, rubro, region);

CREATE TABLE postulacion (
    id                  BIGSERIAL PRIMARY KEY,
    licitacion_id       BIGINT NOT NULL REFERENCES licitacion (id) ON DELETE CASCADE,
    cliente_id          BIGINT NOT NULL,
    fecha_postulacion   DATE NOT NULL DEFAULT CURRENT_DATE,
    propuesta           TEXT NOT NULL,
    estado              VARCHAR(20) NOT NULL CHECK (estado IN ('ENVIADA', 'EN_REVISION', 'ACEPTADA', 'RECHAZADA')),
    UNIQUE (licitacion_id, cliente_id)
);

CREATE INDEX idx_postulacion_cliente ON postulacion (cliente_id);
