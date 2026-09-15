-- ============================================================
-- LicitaWatch — inicialización de PostgreSQL local (una sola vez)
-- ============================================================
-- Ejecutar como el superusuario "postgres" (te pedirá su contraseña,
-- la que definiste al instalar PostgreSQL):
--
--   "C:\Program Files\PostgreSQL\18\bin\psql.exe" -U postgres -f db/init.sql
--
-- Crea el rol de aplicación "licitawatch" (usado por los 4 microservicios
-- con base de datos propia) y las 4 bases de datos, una por microservicio.
-- No hay base de datos compartida entre servicios.
-- ============================================================

DO
$$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'licitawatch') THEN
        CREATE ROLE licitawatch WITH LOGIN PASSWORD 'licitawatch_local_dev';
    END IF;
END
$$;

CREATE DATABASE usuarios_db       OWNER licitawatch;
CREATE DATABASE licitaciones_db   OWNER licitawatch;
CREATE DATABASE ventas_db         OWNER licitawatch;
CREATE DATABASE notificaciones_db OWNER licitawatch;

GRANT ALL PRIVILEGES ON DATABASE usuarios_db       TO licitawatch;
GRANT ALL PRIVILEGES ON DATABASE licitaciones_db   TO licitawatch;
GRANT ALL PRIVILEGES ON DATABASE ventas_db         TO licitawatch;
GRANT ALL PRIVILEGES ON DATABASE notificaciones_db TO licitawatch;
