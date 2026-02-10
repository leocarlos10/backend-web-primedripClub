-- ============================================
-- Modificar Tabla Pago
-- ============================================

-- Actualizar ENUM de métodos de pago
ALTER TABLE pago
MODIFY COLUMN metodo ENUM(
    'TARJETA_CREDITO',
    'TARJETA_DEBITO',
    'PSE',
    'TRANSFERENCIA',
    'EFECTIVO',
    'NEQUI',
    'DAVIPLATA',
    'OTRO'
) NOT NULL;

-- Actualizar ENUM de estados de pago
ALTER TABLE pago
MODIFY COLUMN estado ENUM(
    'PENDIENTE',
    'APROBADO',
    'RECHAZADO',
    'REEMBOLSADO',
    'CANCELADO'
) DEFAULT 'PENDIENTE';

-- Agregar columna para mensajes de error
ALTER TABLE pago
ADD COLUMN mensaje_error TEXT AFTER referencia;

-- Agregar columna para metadata
ALTER TABLE pago
ADD COLUMN metadata JSON COMMENT 'Datos adicionales de la pasarela';

-- Agregar índice para estado (optimización de consultas)
CREATE INDEX idx_pago_estado_nuevo ON pago(estado);
