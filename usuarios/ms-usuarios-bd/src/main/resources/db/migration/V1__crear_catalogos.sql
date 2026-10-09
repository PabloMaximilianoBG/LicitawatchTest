-- ============================================================
-- usuarios-bd · Modelo ER (página 2): tablas de catálogo
-- int -> SERIAL/INTEGER · texto -> VARCHAR · UQ = UNIQUE
-- ============================================================
CREATE TABLE rol (
    id     SERIAL       PRIMARY KEY,
    nombre VARCHAR(50)  NOT NULL UNIQUE
);

CREATE TABLE rubro (
    id     SERIAL       PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE region (
    id     SERIAL       PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE ciudad (
    id        SERIAL       PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    region_id INTEGER      NOT NULL REFERENCES region (id)
);

CREATE TABLE tamano_empresa (
    id     SERIAL      PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);
