CREATE TABLE rol (
    id     BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL UNIQUE CHECK (nombre IN ('EMPRESA', 'CLIENTE', 'ADMINISTRADOR'))
);

-- Las filas de rol se siembran en el arranque de la app (ver RolSeeder),
-- no aqui, para que el mismo codigo de seed sirva en dev (Postgres) y en
-- tests (H2 con esquema generado por Hibernate, sin correr esta migracion).

CREATE TABLE usuario (
    id                BIGSERIAL PRIMARY KEY,
    email             VARCHAR(150) NOT NULL UNIQUE,
    contrasena_hash   VARCHAR(100) NOT NULL,
    rol_id            BIGINT NOT NULL REFERENCES rol (id),
    activo            BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en         TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE empresa (
    id              BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL UNIQUE REFERENCES usuario (id) ON DELETE CASCADE,
    razon_social    VARCHAR(200) NOT NULL,
    rut             VARCHAR(12) NOT NULL UNIQUE,
    rubro           VARCHAR(100)
);

CREATE TABLE cliente (
    id                BIGSERIAL PRIMARY KEY,
    usuario_id        BIGINT NOT NULL UNIQUE REFERENCES usuario (id) ON DELETE CASCADE,
    nombre_contacto   VARCHAR(200) NOT NULL,
    rut               VARCHAR(12) NOT NULL UNIQUE
);

CREATE TABLE administrador (
    id           BIGSERIAL PRIMARY KEY,
    usuario_id   BIGINT NOT NULL UNIQUE REFERENCES usuario (id) ON DELETE CASCADE,
    area         VARCHAR(100)
);

CREATE TABLE refresh_token (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT NOT NULL REFERENCES usuario (id) ON DELETE CASCADE,
    token_hash  VARCHAR(100) NOT NULL UNIQUE,
    expira_en   TIMESTAMP NOT NULL,
    revocado    BOOLEAN NOT NULL DEFAULT FALSE,
    creado_en   TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_token_usuario ON refresh_token (usuario_id);
