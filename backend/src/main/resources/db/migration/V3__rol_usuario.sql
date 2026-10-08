-- Laboratorio 5: la API necesita distinguir al menos dos roles para la autorización por endpoint.
-- Los GO son necesarios: SQL Server compila el batch completo antes de ejecutarlo, así que sin
-- separarlos la referencia a la columna "rol" en los statements siguientes falla con
-- "Invalid column name 'rol'" porque la columna todavía no existe en tiempo de compilación.
ALTER TABLE usuario ADD rol NVARCHAR(20) NOT NULL CONSTRAINT df_usuario_rol DEFAULT 'USUARIO';
GO

ALTER TABLE usuario ADD CONSTRAINT ck_usuario_rol CHECK (rol IN ('USUARIO', 'ADMIN'));
GO

-- Kimberley (seed del Lab 1/2) queda como administradora de ejemplo para poder probar los
-- endpoints restringidos a ADMIN (alta/edición/baja del catálogo de lugares).
UPDATE usuario SET rol = 'ADMIN' WHERE correo = 'kimberley.demo@ejemplo.com';
