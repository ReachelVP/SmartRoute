-- Provincias de Costa Rica
INSERT INTO provincia (nombre) VALUES
    ('San José'), ('Alajuela'), ('Cartago'), ('Heredia'),
    ('Guanacaste'), ('Puntarenas'), ('Limón');

-- Categorías del dominio (Propuesta de Dominio, sección 3)
INSERT INTO categoria (nombre, descripcion) VALUES
    ('Natural', 'Volcanes, parques nacionales, playas, senderos'),
    ('Cultural', 'Museos, galerías, ferias, artesanía'),
    ('Histórica', 'Sitios y edificaciones de valor histórico'),
    ('Social', 'Vida nocturna, gastronomía, eventos comunitarios');

-- Usuarios de ejemplo (password_hash es un valor de relleno, no una contraseña real)
INSERT INTO usuario (nombre, correo, password_hash, provincia_id)
VALUES
    ('Reachel Valverde', 'reachel.demo@ejemplo.com', 'demo-hash-no-real', (SELECT id FROM provincia WHERE nombre = 'San José')),
    ('Kimberley Gómez', 'kimberley.demo@ejemplo.com', 'demo-hash-no-real', (SELECT id FROM provincia WHERE nombre = 'Heredia'));

INSERT INTO perfil_preferencias (usuario_id, presupuesto_maximo, tiene_transporte_propio, radio_distancia_km)
VALUES
    ((SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com'), 20000.00, 0, 40),
    ((SELECT id FROM usuario WHERE correo = 'kimberley.demo@ejemplo.com'), 35000.00, 1, 100);

INSERT INTO perfil_preferencias_categoria (perfil_id, categoria_id)
VALUES
    ((SELECT id FROM perfil_preferencias WHERE usuario_id = (SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com')), (SELECT id FROM categoria WHERE nombre = 'Natural')),
    ((SELECT id FROM perfil_preferencias WHERE usuario_id = (SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com')), (SELECT id FROM categoria WHERE nombre = 'Cultural')),
    ((SELECT id FROM perfil_preferencias WHERE usuario_id = (SELECT id FROM usuario WHERE correo = 'kimberley.demo@ejemplo.com')), (SELECT id FROM categoria WHERE nombre = 'Histórica')),
    ((SELECT id FROM perfil_preferencias WHERE usuario_id = (SELECT id FROM usuario WHERE correo = 'kimberley.demo@ejemplo.com')), (SELECT id FROM categoria WHERE nombre = 'Social'));

-- Lugares reales conocidos, distribuidos entre provincias y categorías
INSERT INTO lugar (nombre, descripcion, provincia_id, categoria_id, costo_estimado, nivel_accesibilidad, requiere_transporte_propio)
VALUES
    ('Volcán Poás', 'Parque nacional con cráter activo y senderos.', (SELECT id FROM provincia WHERE nombre = 'Alajuela'), (SELECT id FROM categoria WHERE nombre = 'Natural'), 8000.00, 'MEDIA', 1),
    ('Catarata La Paz', 'Cascadas y jardín de vida silvestre.', (SELECT id FROM provincia WHERE nombre = 'Alajuela'), (SELECT id FROM categoria WHERE nombre = 'Natural'), 15000.00, 'MEDIA', 1),
    ('Museo Nacional de Costa Rica', 'Historia y arqueología en el antiguo Cuartel Bellavista.', (SELECT id FROM provincia WHERE nombre = 'San José'), (SELECT id FROM categoria WHERE nombre = 'Histórica'), 3000.00, 'ALTA', 0),
    ('Teatro Nacional', 'Edificio histórico y salas de espectáculos.', (SELECT id FROM provincia WHERE nombre = 'San José'), (SELECT id FROM categoria WHERE nombre = 'Cultural'), 2000.00, 'ALTA', 0),
    ('Mercado Central de San José', 'Gastronomía y comercio tradicional.', (SELECT id FROM provincia WHERE nombre = 'San José'), (SELECT id FROM categoria WHERE nombre = 'Social'), 5000.00, 'ALTA', 0),
    ('Basílica de Nuestra Señora de los Ángeles', 'Santuario religioso e histórico.', (SELECT id FROM provincia WHERE nombre = 'Cartago'), (SELECT id FROM categoria WHERE nombre = 'Histórica'), 0.00, 'ALTA', 0),
    ('Ruinas de Cartago', 'Restos de la antigua parroquia colonial.', (SELECT id FROM provincia WHERE nombre = 'Cartago'), (SELECT id FROM categoria WHERE nombre = 'Histórica'), 0.00, 'ALTA', 0),
    ('Playa Jacó', 'Playa con oferta de surf y vida nocturna.', (SELECT id FROM provincia WHERE nombre = 'Puntarenas'), (SELECT id FROM categoria WHERE nombre = 'Natural'), 10000.00, 'MEDIA', 1),
    ('Parque Nacional Manuel Antonio', 'Playas y biodiversidad protegida.', (SELECT id FROM provincia WHERE nombre = 'Puntarenas'), (SELECT id FROM categoria WHERE nombre = 'Natural'), 18000.00, 'BAJA', 1),
    ('Feria del Agricultor de Heredia', 'Productos locales y ambiente comunitario.', (SELECT id FROM provincia WHERE nombre = 'Heredia'), (SELECT id FROM categoria WHERE nombre = 'Social'), 4000.00, 'ALTA', 0);

-- Un itinerario confirmado de ejemplo, con 3 paradas en San José (sin necesidad de transporte propio)
INSERT INTO itinerario (usuario_id, fecha, presupuesto_total_estimado, estado)
VALUES ((SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com'), '2026-08-29', 10000.00, 'CONFIRMADO');

INSERT INTO itinerario_parada (itinerario_id, lugar_id, orden, hora_inicio, hora_fin, costo_estimado)
VALUES
    ((SELECT id FROM itinerario WHERE usuario_id = (SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com') AND fecha = '2026-08-29'),
     (SELECT id FROM lugar WHERE nombre = 'Museo Nacional de Costa Rica'), 1, '09:00', '11:00', 3000.00),
    ((SELECT id FROM itinerario WHERE usuario_id = (SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com') AND fecha = '2026-08-29'),
     (SELECT id FROM lugar WHERE nombre = 'Mercado Central de San José'), 2, '11:30', '13:00', 5000.00),
    ((SELECT id FROM itinerario WHERE usuario_id = (SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com') AND fecha = '2026-08-29'),
     (SELECT id FROM lugar WHERE nombre = 'Teatro Nacional'), 3, '13:30', '15:00', 2000.00);

-- Reseñas de ejemplo
INSERT INTO resena (usuario_id, lugar_id, calificacion, comentario)
VALUES
    ((SELECT id FROM usuario WHERE correo = 'reachel.demo@ejemplo.com'), (SELECT id FROM lugar WHERE nombre = 'Museo Nacional de Costa Rica'), 5, 'Excelente recorrido, muy bien explicado.'),
    ((SELECT id FROM usuario WHERE correo = 'kimberley.demo@ejemplo.com'), (SELECT id FROM lugar WHERE nombre = 'Volcán Poás'), 4, 'Hermoso, pero conviene ir temprano por la neblina.');
