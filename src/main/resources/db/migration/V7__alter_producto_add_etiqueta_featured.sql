-- Añadir columnas para etiquetas y productos destacados
ALTER TABLE producto 
ADD COLUMN etiqueta VARCHAR(50) NULL COMMENT 'Etiqueta del producto: Agotado, Nuevo, Oferta, Destacado, Últimas unidades',
ADD COLUMN is_featured BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indica si el producto está destacado';

-- Crear índices para optimizar búsquedas
CREATE INDEX idx_producto_etiqueta ON producto(etiqueta);
CREATE INDEX idx_producto_is_featured ON producto(is_featured);
