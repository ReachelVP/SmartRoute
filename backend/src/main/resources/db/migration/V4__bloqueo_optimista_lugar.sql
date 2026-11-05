-- Laboratorio 7: bloqueo optimista en Lugar. El catálogo lo editan varios administradores (vía
-- /admin/lugares y vía /api/v1/lugares) -- es el dato del dominio con contención real: dos
-- administradores pueden abrir el mismo lugar para editarlo al mismo tiempo. Las filas ya
-- existentes del seed arrancan en version = 0, igual que cualquier fila nueva.
ALTER TABLE lugar ADD version BIGINT NOT NULL CONSTRAINT df_lugar_version DEFAULT 0;
