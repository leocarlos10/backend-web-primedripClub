# 📋 Migración de Base de Datos - Sistema de Carritos y Pedidos

**Fecha:** 9 de febrero de 2026  
**Objetivo:** Implementar un sistema robusto de carritos de compra y gestión de pedidos que soporte usuarios registrados y anónimos.

---

## 📊 Cambios en el Modelo de Datos

### Nuevas Tablas a Crear:

1. ✅ **Carrito** - Gestión de carritos de compra
2. ✅ **DetalleCarrito** - Items dentro del carrito
3. ✅ **DireccionUsuario** - Múltiples direcciones por usuario

### Tablas a Modificar:

1. 🔄 **Usuario** - Agregar campo de teléfono
2. 🔄 **Pedido** - Mejorar estructura, estados y relación con dirección
3. 🔄 **Pago** - Ampliar estados y métodos

---

## 🚀 Paso 1: Backup de la Base de Datos

**⚠️ IMPORTANTE: Realizar backup ANTES de cualquier cambio**

```bash
# MySQL/MariaDB
mysqldump -u tu_usuario -p nombre_base_datos > backup_$(date +%Y%m%d_%H%M%S).sql

# PostgreSQL
pg_dump -U tu_usuario nombre_base_datos > backup_$(date +%Y%m%d_%H%M%S).sql
```

---

## 🔧 Paso 2: Modificar Tabla Usuario

### Script SQL:

```sql
-- ============================================
-- Agregar campo de teléfono a Usuario
-- ============================================

ALTER TABLE Usuario
ADD COLUMN telefono VARCHAR(20) AFTER email;

-- Crear índice para búsquedas por teléfono
CREATE INDEX idx_usuario_telefono ON Usuario(telefono);

-- Verificar cambios
DESCRIBE Usuario;
```

**Resultado esperado:**

```
+------------------+--------------+------+-----+-------------------+
| Field            | Type         | Null | Key | Default           |
+------------------+--------------+------+-----+-------------------+
| id               | bigint       | NO   | PRI | NULL              |
| nombre           | varchar(150) | NO   |     | NULL              |
| email            | varchar(255) | NO   | UNI | NULL              |
| telefono         | varchar(20)  | YES  | MUL | NULL              |
| password         | varchar(255) | NO   |     | NULL              |
| activo           | tinyint(1)   | YES  |     | 1                 |
| fecha_creacion   | timestamp    | YES  |     | CURRENT_TIMESTAMP |
+------------------+--------------+------+-----+-------------------+
```

**Nota:** Las direcciones se manejarán en una tabla separada `DireccionUsuario` para permitir múltiples direcciones por usuario.

---

## 🆕 Paso 3: Crear Tabla DireccionUsuario

```sql
-- ============================================
-- Tabla: DireccionUsuario
-- ============================================
CREATE TABLE DireccionUsuario (
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
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE,

    -- Índices
    INDEX idx_direccion_usuario (usuario_id),
    INDEX idx_direccion_principal (usuario_id, es_principal),
    INDEX idx_direccion_activa (usuario_id, activa),

    -- Validación: alias único por usuario
    UNIQUE KEY uk_usuario_alias (usuario_id, alias)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMENT='Direcciones de envío de los usuarios (múltiples por usuario)';

-- Trigger para asegurar que solo haya una dirección principal por usuario
DELIMITER $$

CREATE TRIGGER trg_direccion_principal_before_insert
BEFORE INSERT ON DireccionUsuario
FOR EACH ROW
BEGIN
    IF NEW.es_principal = TRUE THEN
        -- Desmarcar otras direcciones principales del mismo usuario
        UPDATE DireccionUsuario
        SET es_principal = FALSE
        WHERE usuario_id = NEW.usuario_id;
    END IF;
END$$

CREATE TRIGGER trg_direccion_principal_before_update
BEFORE UPDATE ON DireccionUsuario
FOR EACH ROW
BEGIN
    IF NEW.es_principal = TRUE AND OLD.es_principal = FALSE THEN
        -- Desmarcar otras direcciones principales del mismo usuario
        UPDATE DireccionUsuario
        SET es_principal = FALSE
        WHERE usuario_id = NEW.usuario_id AND id != NEW.id;
    END IF;
END$$

DELIMITER ;

-- Verificar creación
SHOW CREATE TABLE DireccionUsuario;
SELECT COUNT(*) FROM DireccionUsuario;
```

---

## 🆕 Paso 4: Crear Tabla Carrito

```sql
-- ============================================
-- Tabla: Carrito
-- ============================================
CREATE TABLE Carrito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NULL COMMENT 'NULL si es usuario anónimo',
    session_id VARCHAR(255) NULL COMMENT 'Identificador único para usuarios anónimos',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Claves foráneas
    FOREIGN KEY (usuario_id) REFERENCES Usuario(id) ON DELETE CASCADE,

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
CREATE INDEX idx_carrito_usuario ON Carrito(usuario_id);
CREATE INDEX idx_carrito_session ON Carrito(session_id);
CREATE INDEX idx_carrito_fecha_actualizacion ON Carrito(fecha_actualizacion);

-- Verificar creación
SHOW CREATE TABLE Carrito;
SELECT COUNT(*) FROM Carrito;
```

---

## 🆕 Paso 5: Crear Tabla DetalleCarrito

```sql
-- ============================================
-- Tabla: DetalleCarrito
-- ============================================
CREATE TABLE DetalleCarrito (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL COMMENT 'Precio al momento de agregar',
    fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Claves foráneas
    FOREIGN KEY (carrito_id) REFERENCES Carrito(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES Producto(id) ON DELETE RESTRICT,

    -- Un producto solo puede estar una vez por carrito
    UNIQUE KEY uk_carrito_producto (carrito_id, producto_id),

    -- Validaciones
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio CHECK (precio_unitario >= 0)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Items dentro de cada carrito';

-- Índices para optimizar consultas
CREATE INDEX idx_detalle_carrito ON DetalleCarrito(carrito_id);
CREATE INDEX idx_detalle_producto ON DetalleCarrito(producto_id);

-- Verificar creación
SHOW CREATE TABLE DetalleCarrito;
SELECT COUNT(*) FROM DetalleCarrito;
```

---

## 🔄 Paso 6: Modificar Tabla Pedido

### 6.1 Agregar nuevas columnas

```sql
-- ============================================
-- Modificar Tabla Pedido
-- ============================================

-- Agregar campos de envío, dirección y subtotales
ALTER TABLE Pedido
ADD COLUMN direccion_id BIGINT COMMENT 'Referencia a la dirección usada', dirección al momento del pedido',
ADD COLUMN subtotal DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER total,
ADD COLUMN costo_envio DECIMAL(10,2) NOT NULL DEFAULT 0 AFTER subtotal,
ADD COLUMN notas TEXT AFTER estado,
ADD COLUMN fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER fecha_creacion;

-- Agregar clave foránea a DireccionUsuario
ALTER TABLE Pedido
ADD CONSTRAINT fk_pedido_direccion
    FOREIGN KEY (direccion_id) REFERENCES DireccionUsuario(id) ON DELETE SET NULL;

-- Agregar índices
CREATE INDEX idx_pedido_usuario ON Pedido(usuario_id);
CREATE INDEX idx_pedido_direccion ON Pedido(direccion_id);
CREATE INDEX idx_pedido_estado ON Pedido(estado);
CREATE INDEX idx_pedido_fecha_creacion ON Pedido(fecha_creacion);
```

**Explicación de campos:**

- `direccion_id`: Referencia a la dirección en `DireccionUsuario` (puede ser NULL si se eliminó)
- `direccion_envio_snapshot`: Copia JSON de la dirección completa al momento del pedido (para auditoría e historial inmutable)

### 6.2 Actualizar tipo de dato del campo estado

```sql
-- Cambiar estado a ENUM con más opciones
ALTER TABLE Pedido
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
```

### 6.3 Actualizar registros existentes (si aplica)

```sql
-- Actualizar subtotales de pedidos existentes
UPDATE Pedido
SET subtotal = total,
    costo_envio = 0
WHERE subtotal = 0;

-- Verificar cambios
DESCRIBE Pedido;
SELECT COUNT(*), estado FROM Pedido GROUP BY estado;
```

---

## 🔄 Paso 7: Modificar Tabla Pago

```sql
-- ============================================
-- Modificar Tabla Pago
-- ============================================

-- Actualizar ENUM de métodos de pago
ALTER TABLE Pago
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
ALTER TABLE Pago
MODIFY COLUMN estado ENUM(
    'PENDIENTE',
    'APROBADO',
    'RECHAZADO',
    'REEMBOLSADO',
    'CANCELADO'
) DEFAULT 'PENDIENTE';

-- Agregar columna para mensajes de error
ALTER TABLE Pago
ADD COLUMN mensaje_error TEXT AFTER referencia,
ADD COLUMN metadata JSON AFTER mensaje_error COMMENT 'Datos adicionales de la pasarela';

-- Índices
CREATE INDEX idx_pago_pedido ON Pago(pedido_id);
CREATE INDEX idx_pago_estado ON Pago(estado);
CREATE INDEX idx_pago_fecha ON Pago(fecha_pago);

-- Verificar cambios
DESCRIBE Pago;
```

---

## 🧹 Paso 8: Limpieza de Carritos Abandonados (Task Programada)

```sql
-- ============================================
-- Stored Procedure: Limpiar carritos viejos
-- ============================================

DELIMITER $$

CREATE PROCEDURE limpiar_carritos_abandonados()
BEGIN
    -- Eliminar carritos no actualizados en 30 días
    DELETE FROM Carrito
    WHERE fecha_actualizacion < DATE_SUB(NOW(), INTERVAL 30 DAY);

    SELECT ROW_COUNT() AS carritos_eliminados;
END$$

DELIMITER ;

-- Ejecutar manualmente
CALL limpiar_carritos_abandonados();
```

### Programar ejecución automática (MySQL Event):

```sql
-- Crear evento que se ejecuta diariamente
CREATE EVENT evt_limpiar_carritos
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_DATE + INTERVAL 1 DAY + INTERVAL 3 HOUR  -- 3 AM
DO
    CALL limpiar_carritos_abandonados();

-- Verificar eventos programados
SHOW EVENTS;

-- Habilitar scheduler (si está deshabilitado)
SET GLOBAL event_scheduler = ON;
```

---

## ✅ Paso 9: Validación de la Migración

### 8.1 Verificar todas las tablas

```sql
-- Listar todas las tablas
SHOW TABLES;

-- Verificar estructura de cada tabla
DESCRIBE Usuario;
DESCRIBE DireccionUsuario;
DESCRIBE Carrito;
DESCRIBE DetalleCarrito;
DESCRIBE Pedido;
DESCRIBE DetallePedido;
DESCRIBE Pago;
DESCRIBE Producto;
DESCRIBE Categoria;
DESCRIBE Rol;
DESCRIBE Usuario_Rol;
```

### 8.2 Verificar relaciones (Foreign Keys)

```sql
-- Ver todas las relaciones de foreign keys
SELECT
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'nombre_de_tu_base_datos'
  AND REFERENCED_TABLE_NAME IS NOT NULL
ORDER BY TABLE_NAME, COLUMN_NAME;
```

### 8.3 Verificar índices

```sql
-- Ver todos los índices
SELECT
    TABLE_NAME,
    INDEX_NAME,
    GROUP_CONCAT(COLUMN_NAME ORDER BY SEQ_IN_INDEX) AS COLUMNS,
    INDEX_TYPE,
    NON_UNIQUE
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'nombre_de_tu_base_datos'
GROUP BY TABLE_NAME, INDEX_NAME, INDEX_TYPE, NON_UNIQUE
ORDER BY TABLE_NAME, INDEX_NAME;
```

---

## 🧪 Paso 10: Datos de Prueba

```sql
-- ============================================
-- Insertar datos de prueba
-- ============================================

-- 1. Usuarios de prueba (si no existen)
INSERT INTO Usuario (nombre, email, password, telefono, activo)
VALUES
    ('Juan Pérez', 'juan@test.com', '$2a$10$...hash...', '3001234567', TRUE),
    ('María García', 'maria@test.com', '$2a$10$...hash...', '3109876543', TRUE);

-- 2. Direcciones para los usuarios
INSERT INTO DireccionUsuario (usuario_id, alias, direccion_completa, ciudad, departamento, codigo_postal, telefono_contacto, es_principal, activa)
VALUES
    -- Direcciones de Juan Pérez (usuario_id: 1)
    (1, 'Casa', 'Calle 123 #45-67, Apartamento 501', 'Bogotá', 'Cundinamarca', '110111', '3001234567', TRUE, TRUE),
    (1, 'Oficina', 'Carrera 7 #80-15, Piso 3', 'Bogotá', 'Cundinamarca', '110221', '3001234567', FALSE, TRUE),

    -- Direcciones de María García (usuario_id: 2)
    (2, 'Casa', 'Carrera 43A #10-50, Torre 2, Apto 302', 'Medellín', 'Antioquia', '050001', '3109876543', TRUE, TRUE),
    (2, 'Casa Mamá', 'Calle 50 #70-20', 'Medellín', 'Antioquia', '050010', '3109876543', FALSE, TRUE);

-- 3. Carrito para usuario registrado
INSERT INTO Carrito (usuario_id, session_id)
VALUES (1, NULL);

-- 4. Carrito para usuario anónimo
INSERT INTO Carrito (usuario_id, session_id)
VALUES (NULL, 'session_abc123def456');

-- 5. Items en el carrito
INSERT INTO DetalleCarrito (carrito_id, producto_id, cantidad, precio_unitario)
VALUES
    (1, 1, 2, 59.99),
    (1, 3, 1, 79.99),
    (2, 2, 1, 49.99);

-- 6. Verificar datos de prueba

-- Verificar usuarios y sus direcciones
SELECT
    u.id,
    u.nombre,
    u.email,
    COUNT(d.id) AS total_direcciones,
    GROUP_CONCAT(d.alias) AS direcciones
FROM Usuario u
LEFT JOIN DireccionUsuario d ON u.id = d.usuario_id
GROUP BY u.id, u.nombre, u.email;

-- Verificar carritos
SELECT
    c.id AS carrito_id,
    u.nombre AS usuario,
    c.session_id,
    COUNT(dc.id) AS total_items,
    SUM(dc.cantidad * dc.precio_unitario) AS total
FROM Carrito c
LEFT JOIN Usuario u ON c.usuario_id = u.id
LEFT JOIN DetalleCarrito dc ON c.id = dc.carrito_id
GROUP BY c.id, u.nombre, c.session_id;

-- Verificar dirección principal de cada usuario
SELECT
    u.nombre,
    d.alias,
    d.direccion_completa,
    d.ciudad,
    d.es_principal
FROM Usuario u
JOIN DireccionUsuario d ON u.id = d.usuario_id
WHERE d.es_principal = TRUE;
```

---

## 📝 Paso 11: Actualizar Aplicación Backend

### Checklist de cambios en Spring Boot:

#### Entidades:

- [ ] Crear entidad `Carrito.java`
- [ ] Crear entidad `DetalleCarrito.java`
- [ ] Crear entidad `DireccionUsuario.java`
- [ ] Actualizar entidad `Usuario.java` (agregar telefono, relación con DireccionUsuario)
- [ ] Actualizar entidad `Pedido.java` (agregar direccionId, direccionSnapshot, subtotal, costoEnvio, notas)
- [ ] Actualizar entidad `Pago.java` (nuevos estados y métodos)

#### Repositorios:

- [ ] Crear `CarritoRepository.java`
- [ ] Crear `DetalleCarritoRepository.java`
- [ ] Crear `DireccionUsuarioRepository.java`

#### Servicios:

- [ ] Crear `CarritoService.java`
- [ ] Crear `DireccionService.java`
- [ ] Actualizar `PedidoService.java` (método crearDesdeCarrito con dirección)

#### Controladores:

- [ ] Crear `CarritoController.java`
- [ ] Crear `DireccionController.java` (CRUD de direcciones)

#### Funcionalidades:

- [ ] Agregar validaciones de stock
- [ ] Implementar manejo de sesiones anónimas
- [ ] Implementar selección de dirección en checkout
- [ ] Implementar snapshot de dirección en pedido (JSON)

---

## 🔒 Paso 12: Seguridad y Permisos

```sql
-- ============================================
-- Configurar permisos de base de datos
-- ============================================

-- Crear usuario específico para la aplicación (recomendado)
CREATE USER 'app_primedripclub'@'localhost' IDENTIFIED BY 'contraseña_segura';

-- Otorgar permisos específicos
GRANT SELECT, INSERT, UPDATE, DELETE ON primedripclub.* TO 'app_primedripclub'@'localhost';

-- NO otorgar permisos DROP, ALTER, CREATE en producción
FLUSH PRIVILEGES;
```

---

## 📊 Paso 13: Monitoring y Logs

### Crear tabla de auditoría (opcional):

```sql
-- ============================================
-- Tabla de auditoría para cambios importantes
-- ============================================

CREATE TABLE Auditoria (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tabla VARCHAR(50) NOT NULL,
    accion ENUM('INSERT', 'UPDATE', 'DELETE') NOT NULL,
    registro_id BIGINT NOT NULL,
    usuario_id BIGINT NULL,
    datos_anteriores JSON,
    datos_nuevos JSON,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    INDEX idx_auditoria_tabla (tabla),
    INDEX idx_auditoria_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## ⚠️ Rollback Plan

Si algo sale mal, ejecutar en orden inverso:

```sql
-- 1. Eliminar tabla DetalleCarrito
DROP TABLE IF EXISTS DetalleCarrito;

-- 2. Eliminar tabla Carrito
DROP TABLE IF EXISTS Carrito;

-- 3. Revertir cambios en Pedido
ALTER TABLE Pedido
DROP FOREIGN KEY fk_pedido_direccion;

ALTER TABLE Pedido
DROP COLUMN direccion_id,
DROP COLUMN direccion_envio_snapshot,
DROP COLUMN subtotal,
DROP COLUMN costo_envio,
DROP COLUMN notas,
DROP COLUMN fecha_actualizacion;

-- 4. Eliminar tabla DireccionUsuario
DROP TABLE IF EXISTS DireccionUsuario;

-- 5. Revertir cambios en Usuario
ALTER TABLE Usuario
DROP COLUMN telefono;

-- 5. Restaurar desde backup
-- source backup_YYYYMMDD_HHMMSS.sql
```

---

## 📈 Performance y Optimización

```sql
-- ============================================
-- Analizar rendimiento
-- ============================================

-- Ver tamaño de las tablas
SELECT
    TABLE_NAME AS 'Tabla',
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Tamaño (MB)'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'nombre_de_tu_base_datos'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- Analizar queries lentas
SHOW VARIABLES LIKE 'slow_query_log';
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2; -- Queries que tomen más de 2 segundos
```

---

## ✅ Checklist Final

### Base de Datos:

- [ ] Backup realizado
- [ ] Tabla `Usuario` actualizada (teléfono)
- [ ] Tabla `DireccionUsuario` creada
- [ ] Tabla `Carrito` creada
- [ ] Tabla `DetalleCarrito` creada
- [ ] Tabla `Pedido` actualizada (dirección, subtotales)
- [ ] Tabla `Pago` actualizada
- [ ] Foreign keys configuradas
- [ ] Índices creados
- [ ] Triggers de dirección principal creados
- [ ] Stored procedures creadas
- [ ] Eventos programados
- [ ] Datos de prueba insertados
- [ ] Validación exitosa

### Aplicación:

- [ ] Entidades JPA actualizadas (Usuario, DireccionUsuario, Pedido, Pago)
- [ ] Entidades nuevas creadas (Carrito, DetalleCarrito)
- [ ] Repositorios creados (Carrito, DetalleCarrito, DireccionUsuario)
- [ ] Servicios implementados (CarritoService, DireccionService)
- [ ] Controladores creados (CarritoController, DireccionController)
- [ ] DTOs definidos (DireccionDTO, PedidoConDireccionDTO)
- [ ] Validaciones agregadas
- [ ] Tests unitarios
- [ ] Tests de integración

---

## 📚 Recursos Adicionales

- [Documentación Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [MySQL Performance Tuning](https://dev.mysql.com/doc/refman/8.0/en/optimization.html)
- [Patrones de diseño para e-commerce](https://martinfowler.com/eaaCatalog/)

---

## 📞 Soporte

Si encuentras problemas durante la migración:

1. Verifica que el backup esté completo
2. Revisa los logs de MySQL: `/var/log/mysql/error.log`
3. Ejecuta las queries una por una, no en batch
4. Documenta cualquier error encontrado

---

**Última actualización:** 9 de febrero de 2026  
**Versión:** 1.0  
**Autor:** Sistema PrimeDripClub - Documentación de Migración
