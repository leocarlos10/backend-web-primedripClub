-- Tabla Producto
CREATE TABLE producto (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(150) NOT NULL,
    descripcion     TEXT,
    precio          DECIMAL(12,2) NOT NULL,
    stock           INT NOT NULL DEFAULT 0,
    marca           VARCHAR(100),
    imagen_url      VARCHAR(255),
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    categoria_id    BIGINT NOT NULL,
    fecha_creacion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id) REFERENCES categoria (id)
);

-- Tabla Pedido
CREATE TABLE pedido (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id          BIGINT,               -- NULLABLE: cliente invitado
    email_contacto      VARCHAR(150) NOT NULL,
    nombre_contacto     VARCHAR(150) NOT NULL,
    telefono            VARCHAR(50),
    direccion_envio     VARCHAR(255) NOT NULL,
    total               DECIMAL(12,2) NOT NULL,
    estado              VARCHAR(50) NOT NULL,
    fecha_creacion      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario (id)
);

-- Tabla Pago
CREATE TABLE pago (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id       BIGINT NOT NULL,
    metodo          VARCHAR(50) NOT NULL,
    estado          VARCHAR(50) NOT NULL,
    referencia      VARCHAR(150),
    fecha_pago      TIMESTAMP,
    CONSTRAINT fk_pago_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id)
);

-- Tabla DetallePedido
CREATE TABLE detalle_pedido (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id       BIGINT NOT NULL,
    producto_id     BIGINT NOT NULL,
    cantidad        INT NOT NULL,
    precio_unitario DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detalle_pedido_pedido
        FOREIGN KEY (pedido_id) REFERENCES pedido (id),
    CONSTRAINT fk_detalle_pedido_producto
        FOREIGN KEY (producto_id) REFERENCES producto (id)
);
