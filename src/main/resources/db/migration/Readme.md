# Documentación de Flyway

## ¿Qué es Flyway?

Flyway es una herramienta de migración de bases de datos open-source que permite versionar y migrar esquemas de base de datos de forma controlada y repetible. Funciona como un sistema de control de versiones para tu base de datos.

## Características principales

- **Versionado de base de datos**: Mantiene un historial de todos los cambios aplicados
- **Migraciones automáticas**: Se ejecuta automáticamente al iniciar la aplicación
- **Rollback manual**: Permite revertir cambios si es necesario (en versión Pro)
- **Validación**: Verifica que las migraciones aplicadas coincidan con las del código
- **Multi-base de datos**: Soporta múltiples sistemas de bases de datos

## Configuración en Spring Boot

### 1. Dependencia Maven

Añade la dependencia en tu `pom.xml`:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

Para MySQL específicamente:

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

### 2. Configuración en application.properties

```properties
# Habilitar Flyway (habilitado por defecto)
spring.flyway.enabled=true

# Ubicación de los scripts de migración
spring.flyway.locations=classpath:db/migration

# Esquema de base de datos donde se guardarán las migraciones
spring.flyway.schemas=prime_drip_club

# Crear el schema si no existe
spring.flyway.create-schemas=true

# Baseline al inicio (útil para bases de datos existentes)
spring.flyway.baseline-on-migrate=true

# Validar las migraciones al inicio
spring.flyway.validate-on-migrate=true

# Placeholder para usar en los scripts SQL
spring.flyway.placeholder-replacement=true
spring.flyway.placeholders.database=${spring.datasource.database}
```

### 3. Estructura de directorios

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__create_core_tables.sql
        ├── V2__create_product_and_order_tables.sql
        ├── V3__create_usuario_rol.sql
        └── V4__add_new_column.sql
```

## Convención de nombrado de archivos

### Formato estándar

```
V{VERSION}__{DESCRIPCION}.sql
```

**Componentes:**

- `V`: Prefijo obligatorio para migraciones versionadas
- `{VERSION}`: Número de versión (puede ser X, X.Y, X.Y.Z)
- `__`: Doble guion bajo como separador
- `{DESCRIPCION}`: Descripción en snake_case o con espacios
- `.sql`: Extensión del archivo

### Ejemplos válidos

```
V1__initial_schema.sql
V1.1__add_user_table.sql
V2__create_products.sql
V2.1__alter_products_add_price.sql
V3__create_orders.sql
V10__big_refactoring.sql
V2023.01.15.12.00__migration_with_timestamp.sql
```

### Tipos de migraciones

#### 1. **Versioned Migrations** (V)

- Se ejecutan una sola vez
- Se aplican en orden secuencial
- No se pueden modificar después de aplicarse

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. **Undo Migrations** (U) - Versión Pro

- Permiten revertir cambios
- Mismo número de versión que la migración original

```sql
-- U1__create_users_table.sql
DROP TABLE IF EXISTS users;
```

#### 3. **Repeatable Migrations** (R)

- Se ejecutan cada vez que cambia su checksum
- Útiles para vistas, procedimientos almacenados, funciones

```sql
-- R__create_user_view.sql
CREATE OR REPLACE VIEW active_users AS
SELECT * FROM users WHERE active = true;
```

## Orden de ejecución

1. Flyway crea la tabla `flyway_schema_history` si no existe
2. Lee todos los archivos de migración en `db/migration/`
3. Compara con las migraciones ya aplicadas
4. Ejecuta las nuevas migraciones en orden de versión
5. Registra cada migración exitosa en `flyway_schema_history`

## Tabla flyway_schema_history

Flyway mantiene un registro de todas las migraciones aplicadas:

| Columna          | Descripción                 |
| ---------------- | --------------------------- |
| `installed_rank` | Orden de ejecución          |
| `version`        | Número de versión           |
| `description`    | Descripción de la migración |
| `type`           | Tipo (SQL, JDBC)            |
| `script`         | Nombre del archivo          |
| `checksum`       | Hash del contenido          |
| `installed_by`   | Usuario que ejecutó         |
| `installed_on`   | Fecha de ejecución          |
| `execution_time` | Tiempo de ejecución en ms   |
| `success`        | Estado de la ejecución      |

## Mejores prácticas

### 1. **Nunca modificar migraciones aplicadas**

❌ **NO HACER:**

```sql
-- V1__create_users.sql (ya aplicada)
CREATE TABLE users (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50)  -- Agregar columna aquí
);
```

✅ **HACER:**

```sql
-- V2__add_email_to_users.sql (nueva migración)
ALTER TABLE users ADD COLUMN email VARCHAR(100);
```

### 2. **Usar transacciones cuando sea posible**

```sql
-- V3__update_user_data.sql
START TRANSACTION;

UPDATE users SET status = 'active' WHERE status IS NULL;
ALTER TABLE users MODIFY status VARCHAR(20) NOT NULL DEFAULT 'active';

COMMIT;
```

### 3. **Hacer migraciones pequeñas y atómicas**

- Cada migración debe hacer una cosa específica
- Facilita el debug y mantenimiento
- Permite rollback más granular

### 4. **Incluir datos de prueba en migraciones separadas**

```sql
-- V5__seed_initial_data.sql
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'Administrator role'),
    ('USER', 'Regular user role'),
    ('MODERATOR', 'Moderator role');
```

### 5. **Validar antes de aplicar en producción**

```bash
# Verificar el estado
./mvnw flyway:info

# Validar migraciones
./mvnw flyway:validate

# Aplicar migraciones
./mvnw flyway:migrate
```

### 6. **Usar descripciones claras**

```
V1__create_users_table.sql                    ✅ Claro
V1__initial.sql                               ❌ Vago
V2__add_email_and_phone_to_users.sql         ✅ Descriptivo
V2__changes.sql                               ❌ No descriptivo
```

### 7. **Manejar datos existentes con cuidado**

```sql
-- V6__add_not_null_constraint.sql

-- Primero actualizar datos existentes
UPDATE users SET email = CONCAT(username, '@example.com')
WHERE email IS NULL;

-- Luego agregar la restricción
ALTER TABLE users MODIFY email VARCHAR(100) NOT NULL;
```

## Comandos Maven de Flyway

```bash
# Ver información de migraciones
./mvnw flyway:info

# Aplicar migraciones pendientes
./mvnw flyway:migrate

# Validar migraciones aplicadas
./mvnw flyway:validate

# Limpiar base de datos (¡CUIDADO!)
./mvnw flyway:clean

# Reparar tabla de historial
./mvnw flyway:repair

# Establecer baseline
./mvnw flyway:baseline
```

## Resolución de problemas comunes

### Error: Checksum mismatch

**Causa:** El contenido de una migración aplicada fue modificado

**Solución:**

```bash
# Opción 1: Reparar el historial (si el cambio fue intencional)
./mvnw flyway:repair

# Opción 2: Revertir los cambios en el archivo
git checkout V1__migration.sql
```

### Error: Migration failed

**Causa:** Error de sintaxis SQL o problema de datos

**Solución:**

1. Corregir el archivo de migración
2. Limpiar el registro fallido manualmente:

```sql
DELETE FROM flyway_schema_history WHERE success = false;
```

3. Volver a ejecutar la migración

### Base de datos existente (Legacy)

**Solución:**

```properties
# application.properties
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
```

Esto marca la base de datos existente como versión 0 y aplica solo las nuevas migraciones.

## Ejemplo completo de proyecto

### pom.xml

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-mysql</artifactId>
    </dependency>
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### application.properties

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/prime_drip_club
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

### V1\_\_create_core_tables.sql

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
```

## Integración con CI/CD

### Validar en pipeline

```yaml
# .github/workflows/ci.yml
- name: Validate Flyway migrations
  run: ./mvnw flyway:validate

- name: Run tests
  run: ./mvnw test
```

## Referencias y recursos

- **Documentación oficial**: https://flywaydb.org/documentation/
- **Configuración Spring Boot**: https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html#application-properties.data-migration.spring.flyway
- **GitHub Flyway**: https://github.com/flyway/flyway
- **Comandos CLI**: https://flywaydb.org/documentation/usage/commandline/

## Notas finales

- Flyway es ideal para equipos que necesitan control de versiones de base de datos
- Permite trazabilidad completa de cambios en el schema
- Facilita la colaboración entre desarrolladores
- Reduce errores en despliegues a producción
- Se integra perfectamente con Spring Boot y Maven/Gradle

---

**Última actualización**: Enero 2026
