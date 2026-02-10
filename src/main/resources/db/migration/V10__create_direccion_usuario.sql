-- ============================================
-- Tabla: DireccionUsuario
-- ============================================
CREATE TABLE direccion_usuario (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    alias VARCHAR(50) NOT NULL COMMENT 'Ej: Casa, Oficina, Casa de mamá',
    direccion_completa TEXT NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    departamento VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(10),
    telefono_contacto VARCHAR(20),
    es_principal BOOLEAN DEFAULT FALSE COMMENT 'Dirección por defecto del usuario',
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Claves foráneas
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE,

    -- Índices
    INDEX idx_direccion_usuario (usuario_id),
    INDEX idx_direccion_principal (usuario_id, es_principal),
    INDEX idx_direccion_activa (usuario_id, activa),

    -- Validación: alias único por usuario
    UNIQUE KEY uk_usuario_alias (usuario_id, alias)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Direcciones de envío de los usuarios (múltiples por usuario)';

-- Trigger para asegurar que solo haya una dirección principal por usuario
CREATE TRIGGER trg_direccion_principal_before_insert
BEFORE INSERT ON direccion_usuario
FOR EACH ROW
BEGIN
    IF NEW.es_principal = TRUE THEN
        UPDATE direccion_usuario
        SET es_principal = FALSE
        WHERE usuario_id = NEW.usuario_id;
    END IF;
END;

CREATE TRIGGER trg_direccion_principal_before_update
BEFORE UPDATE ON direccion_usuario
FOR EACH ROW
BEGIN
    IF NEW.es_principal = TRUE AND OLD.es_principal = FALSE THEN
        UPDATE direccion_usuario
        SET es_principal = FALSE
        WHERE usuario_id = NEW.usuario_id AND id != NEW.id;
    END IF;
END;
