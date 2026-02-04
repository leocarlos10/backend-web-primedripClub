-- V8__alter_producto_add_sexo.sql
-- Agregar columna sexo a la tabla productos

ALTER TABLE producto
ADD COLUMN sexo VARCHAR(10) DEFAULT 'Unisex';