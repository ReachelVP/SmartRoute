-- Catálogos
CREATE TABLE provincia (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(60) NOT NULL,
    CONSTRAINT uq_provincia_nombre UNIQUE (nombre)
);

CREATE TABLE categoria (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(60) NOT NULL,
    descripcion NVARCHAR(255) NULL,
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);

-- Usuarios y preferencias
CREATE TABLE usuario (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(120) NOT NULL,
    correo NVARCHAR(160) NOT NULL,
    password_hash NVARCHAR(255) NOT NULL,
    provincia_id BIGINT NOT NULL,
    fecha_registro DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT fk_usuario_provincia FOREIGN KEY (provincia_id) REFERENCES provincia (id)
);

CREATE TABLE perfil_preferencias (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    presupuesto_maximo DECIMAL(10, 2) NOT NULL,
    tiene_transporte_propio BIT NOT NULL DEFAULT 0,
    radio_distancia_km INT NULL,
    actualizado_en DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT uq_perfil_usuario UNIQUE (usuario_id),
    CONSTRAINT fk_perfil_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE,
    CONSTRAINT ck_perfil_presupuesto CHECK (presupuesto_maximo >= 0),
    CONSTRAINT ck_perfil_radio CHECK (radio_distancia_km IS NULL OR radio_distancia_km > 0)
);

-- Relación N:M entre perfil de preferencias y categorías de interés (normaliza 1FN)
CREATE TABLE perfil_preferencias_categoria (
    perfil_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    CONSTRAINT pk_perfil_categoria PRIMARY KEY (perfil_id, categoria_id),
    CONSTRAINT fk_ppc_perfil FOREIGN KEY (perfil_id) REFERENCES perfil_preferencias (id) ON DELETE CASCADE,
    CONSTRAINT fk_ppc_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id) ON DELETE CASCADE
);

-- Catálogo de lugares
CREATE TABLE lugar (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(150) NOT NULL,
    descripcion NVARCHAR(MAX) NULL,
    provincia_id BIGINT NOT NULL,
    categoria_id BIGINT NOT NULL,
    costo_estimado DECIMAL(10, 2) NOT NULL,
    nivel_accesibilidad NVARCHAR(20) NOT NULL,
    requiere_transporte_propio BIT NOT NULL DEFAULT 0,
    activo BIT NOT NULL DEFAULT 1,
    creado_en DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_lugar_provincia FOREIGN KEY (provincia_id) REFERENCES provincia (id),
    CONSTRAINT fk_lugar_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    CONSTRAINT ck_lugar_costo CHECK (costo_estimado >= 0),
    CONSTRAINT ck_lugar_accesibilidad CHECK (nivel_accesibilidad IN ('ALTA', 'MEDIA', 'BAJA'))
);

-- Los filtros del Proceso 1 (recomendación) siempre filtran por provincia y por categoría
CREATE INDEX idx_lugar_provincia ON lugar (provincia_id);
CREATE INDEX idx_lugar_categoria ON lugar (categoria_id);

-- Itinerarios
CREATE TABLE itinerario (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    presupuesto_total_estimado DECIMAL(10, 2) NOT NULL,
    estado NVARCHAR(20) NOT NULL DEFAULT 'BORRADOR',
    creado_en DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_itinerario_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT ck_itinerario_presupuesto CHECK (presupuesto_total_estimado >= 0),
    CONSTRAINT ck_itinerario_estado CHECK (estado IN ('BORRADOR', 'CONFIRMADO'))
);

-- Listar "mis itinerarios" es la consulta más frecuente sobre esta tabla
CREATE INDEX idx_itinerario_usuario ON itinerario (usuario_id);

CREATE TABLE itinerario_parada (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    itinerario_id BIGINT NOT NULL,
    lugar_id BIGINT NOT NULL,
    orden SMALLINT NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    costo_estimado DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_parada_itinerario FOREIGN KEY (itinerario_id) REFERENCES itinerario (id) ON DELETE CASCADE,
    CONSTRAINT fk_parada_lugar FOREIGN KEY (lugar_id) REFERENCES lugar (id),
    CONSTRAINT uq_parada_orden UNIQUE (itinerario_id, orden),
    CONSTRAINT ck_parada_horario CHECK (hora_fin > hora_inicio),
    CONSTRAINT ck_parada_orden CHECK (orden > 0),
    CONSTRAINT ck_parada_costo CHECK (costo_estimado >= 0)
);

-- Traer las paradas de un itinerario es una operación de lectura conjunta muy frecuente
CREATE INDEX idx_parada_itinerario ON itinerario_parada (itinerario_id);

-- Reseñas (se quedan en el modelo relacional: necesitan agregación, ej. promedio por lugar)
CREATE TABLE resena (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    lugar_id BIGINT NOT NULL,
    calificacion SMALLINT NOT NULL,
    comentario NVARCHAR(MAX) NULL,
    creado_en DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_resena_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id),
    CONSTRAINT fk_resena_lugar FOREIGN KEY (lugar_id) REFERENCES lugar (id),
    CONSTRAINT uq_resena_usuario_lugar UNIQUE (usuario_id, lugar_id),
    CONSTRAINT ck_resena_calificacion CHECK (calificacion BETWEEN 1 AND 5)
);

-- Calcular el promedio de calificación de un lugar es la consulta más frecuente sobre esta tabla
CREATE INDEX idx_resena_lugar ON resena (lugar_id);
