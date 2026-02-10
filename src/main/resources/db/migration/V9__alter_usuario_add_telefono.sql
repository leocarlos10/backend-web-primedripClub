-- ============================================
-- Agregar campo de teléfono a Usuario
-- ============================================

ALTER TABLE usuario
ADD COLUMN telefono VARCHAR(20) AFTER email;

-- Crear índice para búsquedas por teléfono
CREATE INDEX idx_usuario_telefono ON usuario(telefono);
