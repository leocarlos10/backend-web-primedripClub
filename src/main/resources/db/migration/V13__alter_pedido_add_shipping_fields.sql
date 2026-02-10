-- ============================================
-- Modificar Tabla Pedido
-- ============================================

-- Agregar campos de envío, dirección y subtotales
ALTER TABLE pedido
ADD COLUMN direccion_id BIGINT COMMENT 'Referencia a la dirección usada',
ADD COLUMN direccion_envio_snapshot JSON COMMENT 'Copia inmutable de la dirección al momento del pedido',
ADD COLUMN subtotal DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER total,
ADD COLUMN costo_envio DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER subtotal,
ADD COLUMN notas TEXT AFTER estado,
ADD COLUMN fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER fecha_creacion;

-- Agregar clave foránea a direccion_usuario
ALTER TABLE pedido
ADD CONSTRAINT fk_pedido_direccion
    FOREIGN KEY (direccion_id) REFERENCES direccion_usuario(id) ON DELETE SET NULL;

-- Agregar índice para estado (optimización de consultas)
CREATE INDEX idx_pedido_estado ON pedido(estado);

-- Cambiar estado a ENUM con más opciones
ALTER TABLE pedido
MODIFY COLUMN estado ENUM(
    'PENDIENTE',           -- Recién creado, esperando pago
    'PAGO_PENDIENTE',      -- Esperando confirmación de pago
    'PAGADO',              -- Pago confirmado
    'PROCESANDO',          -- En preparación
    'ENVIADO',             -- En camino al cliente
    'ENTREGADO',           -- Completado exitosamente
    'CANCELADO',           -- Cancelado por usuario o admin
    'DEVUELTO',            -- Producto devuelto
    'REEMBOLSADO'          -- Dinero devuelto
) DEFAULT 'PENDIENTE';

-- Actualizar subtotales de pedidos existentes
UPDATE pedido
SET subtotal = total,
    costo_envio = 0
WHERE subtotal = 0;
