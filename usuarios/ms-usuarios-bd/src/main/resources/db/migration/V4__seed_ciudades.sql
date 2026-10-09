-- Las 346 comunas de Chile, agrupadas por región (tabla ciudad del ER).
INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Arica', 'Camarones', 'Putre', 'General Lagos'
]) AS c WHERE r.nombre = 'Arica y Parinacota';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Iquique', 'Alto Hospicio', 'Pozo Almonte', 'Camiña', 'Colchane', 'Huara', 'Pica'
]) AS c WHERE r.nombre = 'Tarapacá';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Antofagasta', 'Mejillones', 'Sierra Gorda', 'Taltal', 'Calama', 'Ollagüe', 'San Pedro de Atacama',
    'Tocopilla', 'María Elena'
]) AS c WHERE r.nombre = 'Antofagasta';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Copiapó', 'Caldera', 'Tierra Amarilla', 'Chañaral', 'Diego de Almagro', 'Vallenar', 'Alto del Carmen',
    'Freirina', 'Huasco'
]) AS c WHERE r.nombre = 'Atacama';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'La Serena', 'Coquimbo', 'Andacollo', 'La Higuera', 'Paiguano', 'Vicuña', 'Illapel', 'Canela', 'Los Vilos',
    'Salamanca', 'Ovalle', 'Combarbalá', 'Monte Patria', 'Punitaqui', 'Río Hurtado'
]) AS c WHERE r.nombre = 'Coquimbo';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Valparaíso', 'Casablanca', 'Concón', 'Juan Fernández', 'Puchuncaví', 'Quintero', 'Viña del Mar',
    'Isla de Pascua', 'Los Andes', 'Calle Larga', 'Rinconada', 'San Esteban', 'La Ligua', 'Cabildo', 'Papudo',
    'Petorca', 'Zapallar', 'Quillota', 'La Calera', 'Hijuelas', 'La Cruz', 'Nogales', 'San Antonio', 'Algarrobo',
    'Cartagena', 'El Quisco', 'El Tabo', 'Santo Domingo', 'San Felipe', 'Catemu', 'Llaillay', 'Panquehue',
    'Putaendo', 'Santa María', 'Quilpué', 'Limache', 'Olmué', 'Villa Alemana'
]) AS c WHERE r.nombre = 'Valparaíso';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Santiago', 'Cerrillos', 'Cerro Navia', 'Conchalí', 'El Bosque', 'Estación Central', 'Huechuraba',
    'Independencia', 'La Cisterna', 'La Florida', 'La Granja', 'La Pintana', 'La Reina', 'Las Condes',
    'Lo Barnechea', 'Lo Espejo', 'Lo Prado', 'Macul', 'Maipú', 'Ñuñoa', 'Pedro Aguirre Cerda', 'Peñalolén',
    'Providencia', 'Pudahuel', 'Quilicura', 'Quinta Normal', 'Recoleta', 'Renca', 'San Joaquín', 'San Miguel',
    'San Ramón', 'Vitacura', 'Puente Alto', 'Pirque', 'San José de Maipo', 'Colina', 'Lampa', 'Tiltil',
    'San Bernardo', 'Buin', 'Calera de Tango', 'Paine', 'Melipilla', 'Alhué', 'Curacaví', 'María Pinto',
    'San Pedro', 'Talagante', 'El Monte', 'Isla de Maipo', 'Padre Hurtado', 'Peñaflor'
]) AS c WHERE r.nombre = 'Metropolitana de Santiago';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Rancagua', 'Codegua', 'Coinco', 'Coltauco', 'Doñihue', 'Graneros', 'Las Cabras', 'Machalí', 'Malloa',
    'Mostazal', 'Olivar', 'Peumo', 'Pichidegua', 'Quinta de Tilcoco', 'Rengo', 'Requínoa', 'San Vicente',
    'Pichilemu', 'La Estrella', 'Litueche', 'Marchigüe', 'Navidad', 'Paredones', 'San Fernando', 'Chépica',
    'Chimbarongo', 'Lolol', 'Nancagua', 'Palmilla', 'Peralillo', 'Placilla', 'Pumanque', 'Santa Cruz'
]) AS c WHERE r.nombre = 'Libertador General Bernardo O''Higgins';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Talca', 'Constitución', 'Curepto', 'Empedrado', 'Maule', 'Pelarco', 'Pencahue', 'Río Claro', 'San Clemente',
    'San Rafael', 'Cauquenes', 'Chanco', 'Pelluhue', 'Curicó', 'Hualañé', 'Licantén', 'Molina', 'Rauco',
    'Romeral', 'Sagrada Familia', 'Teno', 'Vichuquén', 'Linares', 'Colbún', 'Longaví', 'Parral', 'Retiro',
    'San Javier', 'Villa Alegre', 'Yerbas Buenas'
]) AS c WHERE r.nombre = 'Maule';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Chillán', 'Bulnes', 'Chillán Viejo', 'El Carmen', 'Pemuco', 'Pinto', 'Quillón', 'San Ignacio', 'Yungay',
    'Quirihue', 'Cobquecura', 'Coelemu', 'Ninhue', 'Portezuelo', 'Ránquil', 'Treguaco', 'San Carlos',
    'Coihueco', 'Ñiquén', 'San Fabián', 'San Nicolás'
]) AS c WHERE r.nombre = 'Ñuble';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Concepción', 'Coronel', 'Chiguayante', 'Florida', 'Hualqui', 'Lota', 'Penco', 'San Pedro de la Paz',
    'Santa Juana', 'Talcahuano', 'Tomé', 'Hualpén', 'Lebu', 'Arauco', 'Cañete', 'Contulmo', 'Curanilahue',
    'Los Álamos', 'Tirúa', 'Los Ángeles', 'Antuco', 'Cabrero', 'Laja', 'Mulchén', 'Nacimiento', 'Negrete',
    'Quilaco', 'Quilleco', 'San Rosendo', 'Santa Bárbara', 'Tucapel', 'Yumbel', 'Alto Biobío'
]) AS c WHERE r.nombre = 'Biobío';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Temuco', 'Carahue', 'Cunco', 'Curarrehue', 'Freire', 'Galvarino', 'Gorbea', 'Lautaro', 'Loncoche',
    'Melipeuco', 'Nueva Imperial', 'Padre Las Casas', 'Perquenco', 'Pitrufquén', 'Pucón', 'Saavedra',
    'Teodoro Schmidt', 'Toltén', 'Vilcún', 'Villarrica', 'Cholchol', 'Angol', 'Collipulli', 'Curacautín',
    'Ercilla', 'Lonquimay', 'Los Sauces', 'Lumaco', 'Purén', 'Renaico', 'Traiguén', 'Victoria'
]) AS c WHERE r.nombre = 'La Araucanía';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Valdivia', 'Corral', 'Lanco', 'Los Lagos', 'Máfil', 'Mariquina', 'Paillaco', 'Panguipulli', 'La Unión',
    'Futrono', 'Lago Ranco', 'Río Bueno'
]) AS c WHERE r.nombre = 'Los Ríos';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Puerto Montt', 'Calbuco', 'Cochamó', 'Fresia', 'Frutillar', 'Los Muermos', 'Llanquihue', 'Maullín',
    'Puerto Varas', 'Castro', 'Ancud', 'Chonchi', 'Curaco de Vélez', 'Dalcahue', 'Puqueldón', 'Queilén',
    'Quellón', 'Quemchi', 'Quinchao', 'Osorno', 'Puerto Octay', 'Purranque', 'Puyehue', 'Río Negro',
    'San Juan de la Costa', 'San Pablo', 'Chaitén', 'Futaleufú', 'Hualaihué', 'Palena'
]) AS c WHERE r.nombre = 'Los Lagos';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Coyhaique', 'Lago Verde', 'Aysén', 'Cisnes', 'Guaitecas', 'Cochrane', 'O''Higgins', 'Tortel',
    'Chile Chico', 'Río Ibáñez'
]) AS c WHERE r.nombre = 'Aysén del General Carlos Ibáñez del Campo';

INSERT INTO ciudad (nombre, region_id)
SELECT c, r.id FROM region r, unnest(ARRAY[
    'Punta Arenas', 'Laguna Blanca', 'Río Verde', 'San Gregorio', 'Cabo de Hornos', 'Antártica', 'Porvenir',
    'Primavera', 'Timaukel', 'Natales', 'Torres del Paine'
]) AS c WHERE r.nombre = 'Magallanes y de la Antártica Chilena';
