-- Seed de catálogos (valores del Modelo ER)
INSERT INTO rol (nombre) VALUES ('Licitador'), ('Pyme'), ('Administrador');

-- Ejemplos de rubro del ER (el Administrador puede agregar más desde su panel)
INSERT INTO rubro (nombre) VALUES ('Construcción'), ('Tecnología'), ('Salud');

INSERT INTO tamano_empresa (nombre) VALUES ('Pequeña'), ('Mediana'), ('Grande');

-- Las 16 regiones de Chile (norte a sur)
INSERT INTO region (nombre) VALUES
    ('Arica y Parinacota'),
    ('Tarapacá'),
    ('Antofagasta'),
    ('Atacama'),
    ('Coquimbo'),
    ('Valparaíso'),
    ('Metropolitana de Santiago'),
    ('Libertador General Bernardo O''Higgins'),
    ('Maule'),
    ('Ñuble'),
    ('Biobío'),
    ('La Araucanía'),
    ('Los Ríos'),
    ('Los Lagos'),
    ('Aysén del General Carlos Ibáñez del Campo'),
    ('Magallanes y de la Antártica Chilena');
