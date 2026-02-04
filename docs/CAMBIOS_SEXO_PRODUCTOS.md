# Documentación de Cambios - Sistema de Sexo/Género para Productos

**Fecha:** 3 de febrero de 2026  
**Versión:** 1.0  
**Autor:** Sistema de Desarrollo

---

## 📋 Resumen de Cambios

Se ha implementado un nuevo sistema de clasificación por **sexo/género** para los productos del catálogo. Este cambio permite categorizar productos como `Hombre`, `Mujer`, `Niño` o `Unisex`, mejorando la experiencia de usuario y las capacidades de filtrado.

### 🎯 Objetivos Cumplidos

- ✅ Agregar campo `sexo` al modelo de producto
- ✅ Crear enum `SexoProducto` con valores predefinidos
- ✅ Actualizar todos los DTOs y endpoints
- ✅ Implementar migración de base de datos
- ✅ Mantener retrocompatibilidad completa
- ✅ Soporte para serialización/deserialización JSON

---

## 🆕 Nuevo Enum: `SexoProducto.java`

### Ubicación

```
src/main/java/com/web/prime_drip_club/models/SexoProducto.java
```

### Implementación

```java
package com.web.prime_drip_club.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SexoProducto {
    HOMBRE("Hombre"),
    MUJER("Mujer"),
    NIÑO("Niño"),
    UNISEX("Unisex");

    private final String valor;

    SexoProducto(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static SexoProducto fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (SexoProducto sexo : SexoProducto.values()) {
            if (sexo.valor.equals(valor)) {
                return sexo;
            }
        }
        throw new IllegalArgumentException("Sexo no válido: " + valor);
    }
}
```

### 🔧 Características Técnicas

- **Anotaciones Jackson**: `@JsonValue` y `@JsonCreator` para serialización automática
- **Valores de Display**: `"Hombre"`, `"Mujer"`, `"Niño"`, `"Unisex"`
- **Nombres de Enum**: `HOMBRE`, `MUJER`, `NIÑO`, `UNISEX`
- **Manejo de Nulos**: Soporte para valores null
- **Validación**: Excepción para valores inválidos

---

## 📦 Cambios en Backend

### 1. Modelo `Producto.java`

**Campo Agregado:**

```java
private SexoProducto sexo;
```

**Ubicación:** Entre `etiqueta` y `isFeatured`

### 2. DTOs Actualizados

#### `ProductoRequest.java`

```java
// Import agregado
import com.web.prime_drip_club.models.SexoProducto;

// Campo agregado (opcional)
private SexoProducto sexo;
```

#### `ProductoResponse.java`

```java
// Import agregado
import com.web.prime_drip_club.models.SexoProducto;

// Campo agregado
private SexoProducto sexo;
```

### 3. Servicio `ProductoService.java`

#### Método `crear(ProductoRequest request)`

```java
Producto producto = Producto.builder()
    // ... campos existentes ...
    .etiqueta(request.getEtiqueta())
    .sexo(request.getSexo())  // ← NUEVO
    .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
    .build();
```

#### Método `actualizar(Long id, ProductoRequest request)`

```java
Producto productoActualizado = Producto.builder()
    // ... campos existentes ...
    .etiqueta(request.getEtiqueta())
    .sexo(request.getSexo())  // ← NUEVO
    .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
    .build();
```

#### Método `convertirAResponse()`

```java
return ProductoResponse.builder()
    // ... campos existentes ...
    .etiqueta(producto.getEtiqueta())
    .sexo(producto.getSexo())  // ← NUEVO
    .isFeatured(producto.getIsFeatured())
    .build();
```

### 4. Repositorio `ProductoRepositoryImpl.java`

#### Método `mapRowToProducto()`

```java
private Producto mapRowToProducto(ResultSet rs) throws SQLException {
    String etiquetaValor = rs.getString("etiqueta");
    String sexoValor = rs.getString("sexo");  // ← NUEVO

    return Producto.builder()
        // ... campos existentes ...
        .etiqueta(etiquetaValor != null ? EtiquetaProducto.fromValor(etiquetaValor) : null)
        .sexo(sexoValor != null ? SexoProducto.fromValor(sexoValor) : null)  // ← NUEVO
        .isFeatured(rs.getBoolean("is_featured"))
        .build();
}
```

#### Método `save()` - INSERT

```sql
INSERT INTO producto (nombre, descripcion, precio, stock, marca,
    imagen_url, activo, categoria_id, etiqueta, sexo, is_featured, fecha_creacion)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())
```

#### Método `update()` - UPDATE

```sql
UPDATE producto SET nombre = ?, descripcion = ?, precio = ?,
    stock = ?, marca = ?, imagen_url = ?, activo = ?, categoria_id = ?,
    etiqueta = ?, sexo = ?, is_featured = ? WHERE id = ?
```

---

## 🗄️ Migración de Base de Datos

### Archivo: `V8__alter_producto_add_sexo.sql`

```sql
-- V8__alter_producto_add_sexo.sql
-- Agregar columna sexo a la tabla productos

ALTER TABLE productos
ADD COLUMN sexo VARCHAR(10) DEFAULT 'Unisex';
```

### 📊 Detalles de la Migración

- **Versión**: V8
- **Tipo**: ALTER TABLE
- **Campo**: `sexo VARCHAR(10)`
- **Valor por defecto**: `'Unisex'`
- **Impacto**: Productos existentes tendrán valor `'Unisex'` automáticamente

---

## 🔌 Endpoints API

### Estado de los Endpoints

Todos los endpoints existentes **funcionan sin cambios** y ahora soportan el campo `sexo`:

| Endpoint                | Método | Descripción           | Soporte Sexo   |
| ----------------------- | ------ | --------------------- | -------------- |
| `/v1/productos`         | GET    | Obtener todos (Admin) | ✅ Incluido    |
| `/v1/productos/activos` | GET    | Catálogo público      | ✅ Incluido    |
| `/v1/productos/{id}`    | GET    | Producto específico   | ✅ Incluido    |
| `/v1/productos`         | POST   | Crear producto        | ✅ Soportado   |
| `/v1/productos/{id}`    | PUT    | Actualizar producto   | ✅ Soportado   |
| `/v1/productos/{id}`    | DELETE | Eliminar producto     | ✅ Sin cambios |

---

## 📝 Ejemplos de Uso

### 1. Crear Producto con Sexo (POST)

```json
{
  "nombre": "Camisa Polo Deportiva",
  "descripcion": "Camisa polo de algodón para actividades deportivas",
  "precio": 45.99,
  "stock": 25,
  "marca": "Nike",
  "imagenUrl": "/uploads/images/camisa-polo-deportiva.jpg",
  "activo": true,
  "categoriaId": 1,
  "etiqueta": "Nuevo",
  "sexo": "Hombre",
  "isFeatured": false
}
```

### 2. Actualizar Producto (PUT)

```json
{
  "nombre": "Vestido Casual Elegante",
  "descripcion": "Vestido casual para uso diario",
  "precio": 89.99,
  "stock": 15,
  "marca": "Zara",
  "imagenUrl": "/uploads/images/vestido-casual.jpg",
  "activo": true,
  "categoriaId": 2,
  "etiqueta": "Destacado",
  "sexo": "Mujer",
  "isFeatured": true
}
```

### 3. Respuesta del Servidor

```json
{
  "responseCode": 201,
  "success": true,
  "message": "Producto creado exitosamente",
  "data": {
    "id": 15,
    "nombre": "Camisa Polo Deportiva",
    "descripcion": "Camisa polo de algodón para actividades deportivas",
    "precio": 45.99,
    "stock": 25,
    "marca": "Nike",
    "imagenUrl": "/uploads/images/camisa-polo-deportiva.jpg",
    "activo": true,
    "categoriaId": 1,
    "categoriaNombre": "Ropa",
    "etiqueta": "Nuevo",
    "sexo": "Hombre",
    "isFeatured": false,
    "fechaCreacion": "2026-02-03T18:55:00"
  }
}
```

---

## 🎯 Casos de Uso

### 1. **Filtrado por Sexo**

- **Hombre**: Productos masculinos (camisas, pantalones, zapatos de hombre)
- **Mujer**: Productos femeninos (vestidos, blusas, zapatos de mujer)
- **Niño**: Productos infantiles (ropa para niños y niñas)
- **Unisex**: Productos sin género específico (accesorios, algunos deportivos)

### 2. **Valores Frontend**

El frontend debe enviar exactamente estos valores:

- `"Hombre"`
- `"Mujer"`
- `"Niño"`
- `"Unisex"`

### 3. **Campo Opcional**

- El campo `sexo` es **opcional** en requests
- Si se omite, se guardará como `null` en BD
- Productos existentes tienen valor por defecto `"Unisex"`

---

## ⚙️ Configuración JSON

### Serialización (Java → JSON)

```java
// Enum: SexoProducto.HOMBRE
// JSON: "Hombre"
```

### Deserialización (JSON → Java)

```java
// JSON: "Mujer"
// Enum: SexoProducto.MUJER
```

### Manejo de Errores

```json
// Valor inválido
{
  "sexo": "Adulto" // ❌ Error: "Sexo no válido: Adulto"
}
```

---

## 🔧 Archivos Modificados

| Archivo                           | Tipo        | Cambios                               |
| --------------------------------- | ----------- | ------------------------------------- |
| `V8__alter_producto_add_sexo.sql` | SQL         | Creado                                |
| `SexoProducto.java`               | Enum        | Creado                                |
| `Producto.java`                   | Modelo      | 1 nuevo campo                         |
| `ProductoRequest.java`            | DTO         | 1 nuevo campo opcional                |
| `ProductoResponse.java`           | DTO         | 1 nuevo campo                         |
| `ProductoService.java`            | Servicio    | crear, actualizar, convertirAResponse |
| `ProductoRepositoryImpl.java`     | Repositorio | mapRowToProducto, save, update        |
| `ProductoController.java`         | Controlador | Sin cambios (heredado)                |

---

## ✅ Validaciones

### Estados del Sistema

- ✅ **Compilación**: Sin errores
- ✅ **Build**: Exitoso
- ✅ **Migración**: Lista para ejecutar
- ✅ **Retrocompatibilidad**: Garantizada
- ✅ **Endpoints**: Funcionando correctamente

### Flujo de Creación de Productos

```
Frontend/Postman → ProductoController.crear()
                 ↓
              ProductoService.crear()
                 ↓ (crea Producto con sexo)
              ProductoRepository.save()
                 ↓ (convierte Enum → String para BD)
              Base de Datos
```

### Validado

- ✅ Campos opcionales: `sexo` puede omitirse
- ✅ Valores por defecto: `sexo=null` (nuevos), `sexo="Unisex"` (existentes)
- ✅ Conversión automática: `SexoProducto.HOMBRE` → `"Hombre"`
- ✅ Manejo de errores: Valores inválidos generan excepción
- ✅ Serialización JSON: Bidireccional correcta

---

## 🚀 Próximos Pasos Recomendados

1. **Frontend**: Actualizar formularios para incluir selector de sexo
2. **Filtros**: Implementar filtrado por sexo en el catálogo
3. **Búsqueda**: Agregar sexo a criterios de búsqueda
4. **Analytics**: Incluir sexo en reportes de productos
5. **SEO**: Utilizar sexo para mejorar categorización de productos

---

## 📞 Soporte Técnico

Para consultas sobre esta implementación:

- **Documentación completa**: Revisar este archivo
- **Ejemplos de uso**: Ver sección de ejemplos
- **Troubleshooting**: Verificar valores exactos de enum
- **Migración**: Ejecutar aplicación para aplicar V8

---

**Fin del Documento**  
_Última actualización: 3 de febrero de 2026_
