-- ============================================================
-- usuarios-bd · Modelo ER (página 2): identidad + perfiles
-- Campos con '?' en el ER -> NULL; el resto NOT NULL.
-- ============================================================
CREATE TABLE usuario (
    id         SERIAL       PRIMARY KEY,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    rol_id     INTEGER      NOT NULL REFERENCES rol (id),
    activo     BOOLEAN      NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE administrador (
    id         SERIAL       PRIMARY KEY,
    usuario_id INTEGER      NOT NULL UNIQUE REFERENCES usuario (id),
    nombre     VARCHAR(150) NOT NULL,
    area       VARCHAR(150) NOT NULL
);

CREATE TABLE licitador (
    id                  SERIAL       PRIMARY KEY,
    usuario_id          INTEGER      NOT NULL UNIQUE REFERENCES usuario (id),
    razon_social        VARCHAR(200) NOT NULL,
    rut                 VARCHAR(12)  NOT NULL UNIQUE,
    nombre_contacto     VARCHAR(150) NOT NULL,
    email_contacto      VARCHAR(255) NOT NULL,
    telefono            VARCHAR(20)  NOT NULL,
    rubro_id            INTEGER      NOT NULL REFERENCES rubro (id),
    ciudad_id           INTEGER      NOT NULL REFERENCES ciudad (id),
    descripcion_empresa TEXT         NOT NULL,
    sitio_web           VARCHAR(255),
    updated_at          TIMESTAMP    NOT NULL
);

CREATE TABLE pyme (
    id                  SERIAL       PRIMARY KEY,
    usuario_id          INTEGER      NOT NULL UNIQUE REFERENCES usuario (id),
    razon_social        VARCHAR(200) NOT NULL,
    rut                 VARCHAR(12)  NOT NULL UNIQUE,
    nombre_contacto     VARCHAR(150) NOT NULL,
    email_contacto      VARCHAR(255) NOT NULL,
    telefono            VARCHAR(20)  NOT NULL,
    rubro_id            INTEGER      NOT NULL REFERENCES rubro (id),
    ciudad_id           INTEGER      NOT NULL REFERENCES ciudad (id),
    tamano_empresa_id   INTEGER      NOT NULL REFERENCES tamano_empresa (id),
    descripcion_empresa TEXT         NOT NULL,
    sitio_web           VARCHAR(255),
    updated_at          TIMESTAMP    NOT NULL
);
