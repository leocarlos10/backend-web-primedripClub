# Documentación de Cambios - Sistema de Etiquetas y Productos Destacados

**Fecha:** Febrero 3, 2026  
**Versión:** 1.1  
**Estado:** ✅ Validado y Funcional

---

## 📋 Resumen de Cambios

Se han implementado dos nuevos atributos **opcionales** en la gestión de productos para mejorar el catálogo:

1. **Etiqueta de Producto** - Enum para clasificar productos según su estado
2. **Producto Destacado** - Boolean para marcar productos como destacados en el catálogo

**✅ Retrocompatibilidad Garantizada:** Los productos existentes siguen funcionando sin necesidad de modificaciones.

---

## 🗄️ Cambios en Base de Datos

### Archivo de Migración: `V7__alter_producto_add_etiqueta_featured.sql`

Se agregaron dos nuevas columnas a la tabla `producto`:

#### 1. Columna `etiqueta` (VARCHAR(50), NULLABLE)

- **Tipo**: VARCHAR(50)
- **Permite NULL**: ✅ Sí (campo opcional)
- **Valores permitidos**:
  - `"Agotado"` - Producto sin stock
  - `"Nuevo"` - Producto recién agregado
  - `"Oferta"` - Producto en promoción/descuento
  - `"Destacado"` - Producto destacado en el catálogo
  - `"Últimas unidades"` - Stock bajo
  - `NULL` - Sin etiqueta (por defecto)

#### 2. Columna `is_featured` (BOOLEAN, NOT NULL, DEFAULT FALSE)

- **Tipo**: BOOLEAN
- **Valor por defecto**: FALSE
- **Permite NULL**: ❌ No (siempre tiene valor)
- **Propósito**: Indicar si el producto debe aparecer como destacado

#### Índices Creados (Optimización de Rendimiento)

```sql
CREATE INDEX idx_producto_etiqueta ON producto(etiqueta);
CREATE INDEX idx_producto_is_featured ON producto(is_featured);
```

**¿Por qué índices?** Aceleran las consultas de filtrado en el catálogo (ej: mostrar solo productos nuevos o destacados).

---

## 📦 Cambios en Backend

### 1. Nuevo Enum: `EtiquetaProducto.java`

**Ubicación:** `src/main/java/com/web/prime_drip_club/models/EtiquetaProducto.java`

```java
public enum EtiquetaProducto {
    AGOTADO("Agotado"),
    NUEVO("Nuevo"),
    OFERTA("Oferta"),
    DESTACADO("Destacado"),
    ULTIMAS_UNIDADES("Últimas unidades");
}
```

**Métodos incluidos:**

- `getValor()` - Obtiene el valor en español del enum
- `fromValor(String valor)` - Convierte un string al enum correspondiente

---

### 2. Modelo `Producto.java`

**Cambios realizados:**

- Agregado campo: `private EtiquetaProducto etiqueta;`
- Agregado campo: `private Boolean isFeatured;`

**Ejemplo de uso:**

```java
Producto producto = Producto.builder()
    .nombre("Zapatillas Nike")
    .etiqueta(EtiquetaProducto.NUEVO)
    .isFeatured(true)
    .build();
```

---

### 3. DTOs

#### ProductoRequest.java

- Agregado: `private EtiquetaProducto etiqueta;`
- Agregado: `private Boolean isFeatured;`
- **Validación**: Ambos campos son opcionales (null)

#### ProductoResponse.java

- Agregado: `private EtiquetaProducto etiqueta;`
- Agregado: `private Boolean isFeatured;`

---

### 4. Repositorio: `ProductoRepositoryImpl.java`

#### Cambios en `mapRowToProducto()`

```java
String etiquetaValor = rs.getString("etiqueta");
// ...
.etiqueta(etiquetaValor != null ? EtiquetaProducto.fromValor(etiquetaValor) : null)
.isFeatured(rs.getBoolean("is_featured"))
```

#### Cambios en método `save()`

- Se agregaron dos parámetros al INSERT:
  - Posición 9: `etiqueta` (String, convertido desde el enum)
  - Posición 10: `is_featured` (Boolean, default false)

#### Cambios en método `update()`

- Se agregaron dos campos al UPDATE:
  - `etiqueta = ?`
  - `is_featured = ?`

---

### 5. Servicio: `ProductoService.java`

#### Método `crear(ProductoRequest request)`

```java
Producto producto = Producto.builder()
    // ... campos existentes ...
    .etiqueta(request.getEtiqueta())
    .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
    .build();
```

#### Método `actualizar(Long id, ProductoRequest request)`

```java
Producto productoActualizado = Producto.builder()
    // ... campos existentes ...
    .etiqueta(request.getEtiqueta())
    .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
    .build();
```

#### Método `convertirAResponse()`

- Mapea los nuevos campos etiqueta e isFeatured a la respuesta

---

## 🔌 Endpoints API

### Crear Producto (POST)

```bash
POST /v1/productos
Content-Type: application/json

{
  "nombre": "Producto Premium",
  "descripcion": "Descripción del producto",
  "precio": 99.99,
  "stock": 10,
  "marca": "Nike",
  "imagenUrl": "http://example.com/image.jpg",
  "activo": true,
  "categoriaId": 1,
  "etiqueta": "NUEVO",
  "isFeatured": true
}
```

### Actualizar Producto (PUT)

```bash
PUT /v1/productos/{id}
Content-Type: application/json

{
  "nombre": "Producto Actualizado",
  "descripcion": "Nueva descripción",
  "precio": 79.99,
  "stock": 5,
  "marca": "Nike",
  "imagenUrl": "http://example.com/new-image.jpg",
  "activo": true,
  "categoriaId": 1,
  "etiqueta": "OFERTA",
  "isFeatured": false
}
```

### Obtener Productos (GET)

```bash
GET /v1/productos
GET /v1/productos/activos
GET /v1/productos/{id}
```

---

## ✅ Validación y Testing

### Errores de Compilación

- **Estado**: ✅ Sin errores
- **Build**: Exitoso

### Casos de Uso Validados

1. **Crear Producto con Etiqueta**
   - Guardar producto con etiqueta NUEVO
   - Verificar que se mapea correctamente en BD

2. **Actualizar Etiqueta**
   - Cambiar de NUEVO a OFERTA
   - Verificar actualización en BD

3. **Productos Destacados**
   - Marcar producto como destacado
   - Usar para filtros en catálogo

4. **Campos Opcionales**
   - Crear producto sin etiqueta (null permitido)
   - Crear producto sin isFeatured (default false)

---

## 🚀 Deployment

### Pasos para Aplicar Cambios

1. **Ejecutar migración SQL**

   ```bash
   # La migración V7 se ejecutará automáticamente con Flyway
   # al iniciar la aplicación
   ```

2. **Compilar proyecto**

   ```bash
   mvn clean package
   ```

3. **Reiniciar aplicación**
   ```bash
   mvn spring-boot:run
   ```

---

## 📊 Compatibilidad

- ✅ Backward compatible - Productos existentes tendrán:
  - `etiqueta = NULL`
  - `isFeatured = FALSE`
- ✅ Migraciones automáticas con Flyway
- ✅ Sin cambios en endpoints existentes

---

## 🔍 Consultas SQL Útiles

### Obtener productos destacados

```sql
SELECT * FROM producto WHERE is_featured = TRUE;
```

### Obtener productos con etiqueta específica

```sql
SELECT * FROM producto WHERE etiqueta = 'Nuevo';
```

### Obtener productos nuevos y destacados

```sql
SELECT * FROM producto
WHERE etiqueta = 'Nuevo' OR is_featured = TRUE;
```

### Contar productos por etiqueta

```sql
SELECT etiqueta, COUNT(*) as cantidad
FROM producto
GROUP BY etiqueta;
```

---

## 📝 Notas Técnicas

- Los valores del enum se almacenan como strings en BD para legibilidad
- El enum `EtiquetaProducto` es de solo lectura (immutable)
- La conversión enum ↔ string es automática en repositorio
- Los índices mejoran performance en filtros por etiqueta
- Manejo de null seguro en conversiones

---

## 🔧 Archivos Modificados

| Archivo                                        | Tipo        | Cambios                               |
| ---------------------------------------------- | ----------- | ------------------------------------- |
| `V7__alter_producto_add_etiqueta_featured.sql` | SQL         | Creado                                |
| `EtiquetaProducto.java`                        | Enum        | Creado                                |
| `Producto.java`                                | Modelo      | 2 nuevos campos                       |
| `ProductoRequest.java`                         | DTO         | 2 nuevos campos opcionales            |
| `ProductoResponse.java`                        | DTO         | 2 nuevos campos                       |
| `ProductoRepositoryImpl.java`                  | Repositorio | mapRowToProducto, save, update        |
| `ProductoService.java`                         | Servicio    | crear, actualizar, convertirAResponse |
| `ProductoController.java`                      | Controlador | Sin cambios (heredado)                |

---

## ✅ Revisión de Flujo Completo

### Estado del Sistema

- ✅ **Compilación**: Sin errores
- ✅ **Build**: Exitoso
- ✅ **Retrocompatibilidad**: Garantizada

### Flujo de Creación de Productos

```
Frontend/Postman → ProductoController.crear()
                 ↓
              ProductoService.crear()
                 ↓ (crea Producto con etiqueta e isFeatured)
              ProductoRepository.save()
                 ↓ (convierte Enum → String para BD)
              Base de Datos
```

**✅ Validado:**

- Campos opcionales: `etiqueta` y `isFeatured` pueden omitirse
- Valores por defecto: `etiqueta=null`, `isFeatured=false`
- Conversión automática: `EtiquetaProducto.NUEVO` → `"Nuevo"`

### Flujo de Actualización de Productos

```
Frontend/Postman → ProductoController.actualizar()
                 ↓
              ProductoService.actualizar()
                 ↓ (actualiza Producto con nuevos valores)
              ProductoRepository.update()
                 ↓ (actualiza etiqueta e is_featured en BD)
              Base de Datos
```

**✅ Validado:**

- Actualización de etiqueta: permite cambiar o establecer a `null`
- Actualización de isFeatured: permite cambiar entre `true/false`
- Productos existentes: pueden ser actualizados sin errores

### Flujo de Consulta de Productos

```
Frontend/Postman → ProductoController.obtenerTodos()/obtenerPorId()
                 ↓
              ProductoService.obtenerXXX()
                 ↓
              ProductoRepository.findXXX()
                 ↓ (mapea String de BD → Enum Java)
              ProductoService.convertirAResponse()
                 ↓
              ProductoResponse (incluye etiqueta e isFeatured)
```

**✅ Validado:**

- Mapeo correcto: `"Nuevo"` (BD) → `EtiquetaProducto.NUEVO` (Java)
- Manejo de null: productos sin etiqueta devuelven `null`
- Índices funcionando: consultas rápidas por etiqueta/featured

### Compatibilidad con Datos Existentes

**Productos creados ANTES de la migración V7:**

- ✅ Consultas funcionan correctamente
- ✅ Devuelven `etiqueta: null` e `isFeatured: false`
- ✅ Pueden ser actualizados sin problemas
- ✅ No requieren migración de datos

**Productos creados DESPUÉS de la migración V7:**

- ✅ Pueden crearse sin etiqueta (opcional)
- ✅ Pueden crearse con etiqueta específica
- ✅ isFeatured tiene valor por defecto `false`

---

## ⚠️ Consideraciones Futuras

1. **Filtros avanzados** - Implementar endpoint para filtrar por etiqueta
2. **Búsqueda** - Incluir etiqueta en búsquedas full-text
3. **Analytics** - Rastrear productos más etiquetados
4. **Caché** - Cachear productos destacados

---

**Versión del documento:** 1.1  
**Última actualización:** Febrero 3, 2026  
**Estado de revisión:** ✅ Flujo completo validado
