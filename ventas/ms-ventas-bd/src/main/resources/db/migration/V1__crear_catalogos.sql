-- ============================================================
-- ventas-bd · Modelo ER (página 4): tablas de catálogo
-- ============================================================
CREATE TABLE plan_suscripcion (
    id     SERIAL      PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estado_suscripcion (
    id     SERIAL      PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estado_pago (
    id     SERIAL      PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE metodo_pago (
    id     SERIAL      PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);
