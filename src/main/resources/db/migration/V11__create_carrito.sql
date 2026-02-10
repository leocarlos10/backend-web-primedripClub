-- ============================================
-- Tabla: carrito
-- ============================================
CREATE TABLE carrito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NULL COMMENT 'NULL si es usuario anónimo',
    session_id VARCHAR(255) NULL COMMENT 'Identificador único para usuarios anónimos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Claves foráneas
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,

    -- Índices únicos
    UNIQUE KEY uk_carrito_usuario (usuario_id),
    UNIQUE KEY uk_carrito_session (session_id),

    -- Validación: debe tener usuario_id O session_id
    CONSTRAINT chk_carrito_identificador
        CHECK (
            (usuario_id IS NOT NULL AND session_id IS NULL) OR
            (usuario_id IS NULL AND session_id IS NOT NULL)
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Carritos de compra para usuarios registrados y anónimos';

-- Índices para optimizar búsquedas
CREATE INDEX idx_carrito_usuario ON carrito(usuario_id);
CREATE INDEX idx_carrito_session ON carrito(session_id);
CREATE INDEX idx_carrito_fecha_actualizacion ON carrito(fecha_actualizacion);
