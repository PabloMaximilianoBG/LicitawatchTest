-- ============================================================
-- LicitaWatch v2: creación de las 5 bases de datos (una por microservicio .bd)
-- Ejecutar conectado como superusuario:
--   psql -U postgres -h localhost -f db/crear-bases.sql
-- Las TABLAS y los catálogos los crea Flyway al levantar cada MS.<dominio>.bd.
-- MS.asistente no tiene base de datos.
-- ============================================================
SELECT 'CREATE DATABASE usuarios_bd ENCODING ''UTF8'''
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'usuarios_bd')\gexec

SELECT 'CREATE DATABASE licitaciones_bd ENCODING ''UTF8'''
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'licitaciones_bd')\gexec

SELECT 'CREATE DATABASE ventas_bd ENCODING ''UTF8'''
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ventas_bd')\gexec

SELECT 'CREATE DATABASE notificaciones_bd ENCODING ''UTF8'''
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notificaciones_bd')\gexec

SELECT 'CREATE DATABASE chat_bd ENCODING ''UTF8'''
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'chat_bd')\gexec
