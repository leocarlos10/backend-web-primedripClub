-- ============================================
-- Tabla: DetalleCarrito
-- ============================================
CREATE TABLE detalle_carrito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL COMMENT 'Precio al momento de agregar',
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Claves foráneas
    FOREIGN KEY (carrito_id) REFERENCES carrito(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT,

    -- Un producto solo puede estar una vez por carrito
    UNIQUE KEY uk_carrito_producto (carrito_id, producto_id),

    -- Validaciones
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Items dentro de cada carrito';

-- Índices para optimizar consultas
CREATE INDEX idx_detalle_carrito ON detalle_carrito(carrito_id);
CREATE INDEX idx_detalle_producto ON detalle_carrito(producto_id);
