# 📚 Guía de Implementación Backend - Upload de Imágenes y Gestión de Productos

## Stack: React + Spring Boot + MySQL + JDBC Template

---

## 🎯 Flujo Completo

```
1. Frontend sube imagen → Backend guarda imagen → Retorna URL
2. Frontend envía datos del producto (incluyendo URL de imagen) → Backend guarda en DB
```

---

## 📋 Implementación Paso a Paso

### PASO 1: Configurar Spring Boot para archivos

**Archivo:** `src/main/resources/application.properties`

```properties
spring.application.name=prime_drip_club
server.port=8080

# Conexion a MYSQL
spring.datasource.url=jdbc:mysql://localhost:3306/primedrip_club_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=leocarlos10
spring.datasource.password=Leo10@;
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Flyway - Migraciones
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration
spring.flyway.clean-disabled=false

# Configuración de JWT
jwt.secret.key=586E3272357538782F413F4428472B4B6250655368566D597133743677397A24
jwt.expiration.time=86400000

# ⬇️ CONFIGURACIONES PARA UPLOAD DE IMÁGENES
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Directorio de uploads
file.upload-dir=uploads/images
```

---

### PASO 2: Crear servicio de Upload

**Archivo:** `src/main/java/com/web/prime_drip_club/service/FileStorageService.java`

```java
package com.web.prime_drip_club.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    /**
     * Guarda una imagen en el servidor y retorna la URL relativa
     */
    public String saveImage(MultipartFile file) throws IOException {
        // 1. Validar que sea una imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen (JPG, PNG, GIF, WEBP)");
        }

        // 2. Validar tamaño máximo (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar 5MB");
        }

        // 3. Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 4. Generar nombre único para evitar colisiones
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null ?
            originalFileName.substring(originalFileName.lastIndexOf(".")) : ".jpg";
        String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID() + fileExtension;

        // 5. Guardar archivo en el sistema
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 6. Retornar URL relativa que se guardará en la BD
        return "/uploads/images/" + fileName;
    }

    /**
     * Elimina una imagen del servidor
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl != null && imageUrl.startsWith("/uploads/images/")) {
            String fileName = imageUrl.substring("/uploads/images/".length());
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
        }
    }
}
```

---

### PASO 3: Crear controlador de Upload

**Archivo:** `src/main/java/com/web/prime_drip_club/controllers/FileUploadController.java`

```java
package com.web.prime_drip_club.controllers;

import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Endpoint para subir imágenes de productos
     */
    @PostMapping("/product-image")
    public ResponseEntity<Response<Map<String, String>>> uploadProductImage(
            @RequestParam("image") MultipartFile file) {
        try {
            String imageUrl = fileStorageService.saveImage(file);

            Map<String, String> data = new HashMap<>();
            data.put("imageUrl", imageUrl);

            Response<Map<String, String>> response = Response.<Map<String, String>>builder()
                    .responseCode(200)
                    .success(true)
                    .message("Imagen subida exitosamente")
                    .data(data)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            Response<Map<String, String>> response = Response.<Map<String, String>>builder()
                    .responseCode(400)
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            Response<Map<String, String>> response = Response.<Map<String, String>>builder()
                    .responseCode(500)
                    .success(false)
                    .message("Error al subir la imagen: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
```

---

### PASO 4: Configurar acceso a archivos estáticos

**Archivo:** `src/main/java/com/web/prime_drip_club/config/CorsConfig.java`

```java
package com.web.prime_drip_club.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization", "Content-Type"));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Permite servir archivos estáticos desde /uploads/images/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/images/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
```

---

### PASO 5: Crear DTOs para Producto

**Archivo:** `src/main/java/com/web/prime_drip_club/dto/Producto/ProductoRequest.java`

```java
package com.web.prime_drip_club.dto.Producto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 1000, message = "La descripción no puede superar 1000 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 100, message = "La marca no puede superar 100 caracteres")
    private String marca;

    @NotBlank(message = "La URL de la imagen es obligatoria")
    @Size(max = 255, message = "La URL no puede superar 255 caracteres")
    private String imagenUrl;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;
}
```

**Archivo:** `src/main/java/com/web/prime_drip_club/dto/Producto/ProductoResponse.java`

```java
package com.web.prime_drip_club.dto.Producto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductoResponse {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String marca;
    private String imagenUrl;
    private Boolean activo;
    private Integer categoriaId;
    private String categoriaNombre; // Nombre de la categoría para el frontend
    private LocalDateTime fechaCreacion;
}
```

---

### PASO 6: Crear Repositorio de Producto

**Archivo:** `src/main/java/com/web/prime_drip_club/repository/ProductoRepository.java`

```java
package com.web.prime_drip_club.repository;

import com.web.prime_drip_club.models.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> findAll();
    List<Producto> findByActivo(Boolean activo);
    List<Producto> findByCategoriaId(Integer categoriaId);
    Optional<Producto> findById(Integer id);
    Integer save(Producto producto);
    Boolean update(Producto producto);
    Boolean delete(Integer id);
}
```

---

### PASO 7: Implementar Repositorio

**Archivo:** `src/main/java/com/web/prime_drip_club/repository/impl/ProductoRepositoryImpl.java`

```java
package com.web.prime_drip_club.repository.impl;

import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.models.Producto;
import com.web.prime_drip_club.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductoRepositoryImpl implements ProductoRepository {

    private final JdbcTemplate jdbcTemplate;

    private Producto mapRowToProducto(ResultSet rs) throws SQLException {
        return Producto.builder()
                .id(rs.getInt("id"))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .precio(rs.getBigDecimal("precio"))
                .stock(rs.getInt("stock"))
                .marca(rs.getString("marca"))
                .imagenUrl(rs.getString("imagen_url"))
                .activo(rs.getBoolean("activo"))
                .categoriaId(rs.getInt("categoria_id"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime())
                .build();
    }

    @Override
    public List<Producto> findAll() {
        String sql = "SELECT * FROM producto ORDER BY fecha_creacion DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProducto(rs));
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener productos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> findByActivo(Boolean activo) {
        String sql = "SELECT * FROM producto WHERE activo = ? ORDER BY fecha_creacion DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProducto(rs), activo);
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener productos activos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> findByCategoriaId(Integer categoriaId) {
        String sql = "SELECT * FROM producto WHERE categoria_id = ? ORDER BY fecha_creacion DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProducto(rs), categoriaId);
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener productos por categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Producto> findById(Integer id) {
        String sql = "SELECT * FROM producto WHERE id = ?";
        try {
            Producto producto = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToProducto(rs), id);
            return Optional.ofNullable(producto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Integer save(Producto producto) {
        String sql = "INSERT INTO producto (nombre, descripcion, precio, stock, marca, " +
                     "imagen_url, activo, categoria_id, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, producto.getNombre());
                ps.setString(2, producto.getDescripcion());
                ps.setBigDecimal(3, producto.getPrecio());
                ps.setInt(4, producto.getStock());
                ps.setString(5, producto.getMarca());
                ps.setString(6, producto.getImagenUrl());
                ps.setBoolean(7, producto.getActivo());
                ps.setInt(8, producto.getCategoriaId());
                return ps;
            }, keyHolder);

            return keyHolder.getKey().intValue();
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean update(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, descripcion = ?, precio = ?, " +
                     "stock = ?, marca = ?, imagen_url = ?, activo = ?, categoria_id = ? " +
                     "WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql,
                    producto.getNombre(),
                    producto.getDescripcion(),
                    producto.getPrecio(),
                    producto.getStock(),
                    producto.getMarca(),
                    producto.getImagenUrl(),
                    producto.getActivo(),
                    producto.getCategoriaId(),
                    producto.getId());
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean delete(Integer id) {
        String sql = "DELETE FROM producto WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }
}
```

---

### PASO 8: Crear Servicio de Producto

**Archivo:** `src/main/java/com/web/prime_drip_club/service/ProductoService.java`

```java
package com.web.prime_drip_club.service;

import com.web.prime_drip_club.dto.Producto.ProductoRequest;
import com.web.prime_drip_club.dto.Producto.ProductoResponse;
import com.web.prime_drip_club.exception.ValidationException;
import com.web.prime_drip_club.models.Categoria;
import com.web.prime_drip_club.models.Producto;
import com.web.prime_drip_club.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final FileStorageService fileStorageService;
    private final JdbcTemplate jdbcTemplate;

    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public List<ProductoResponse> obtenerActivos() {
        return productoRepository.findByActivo(true).stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    public ProductoResponse obtenerPorId(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Producto no encontrado con ID: " + id));
        return convertirAResponse(producto);
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .marca(request.getMarca())
                .imagenUrl(request.getImagenUrl())
                .activo(request.getActivo())
                .categoriaId(request.getCategoriaId())
                .build();

        Integer id = productoRepository.save(producto);
        producto.setId(id);

        return convertirAResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Producto no encontrado con ID: " + id));

        // Si cambió la imagen, eliminar la anterior
        if (!productoExistente.getImagenUrl().equals(request.getImagenUrl())) {
            try {
                fileStorageService.deleteImage(productoExistente.getImagenUrl());
            } catch (Exception e) {
                System.err.println("Error al eliminar imagen anterior: " + e.getMessage());
            }
        }

        Producto productoActualizado = Producto.builder()
                .id(id)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .marca(request.getMarca())
                .imagenUrl(request.getImagenUrl())
                .activo(request.getActivo())
                .categoriaId(request.getCategoriaId())
                .fechaCreacion(productoExistente.getFechaCreacion())
                .build();

        productoRepository.update(productoActualizado);
        return convertirAResponse(productoActualizado);
    }

    @Transactional
    public void eliminar(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Producto no encontrado con ID: " + id));

        // Eliminar imagen
        try {
            fileStorageService.deleteImage(producto.getImagenUrl());
        } catch (Exception e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
        }

        productoRepository.delete(id);
    }

    private ProductoResponse convertirAResponse(Producto producto) {
        // Obtener nombre de la categoría
        String categoriaNombre = obtenerNombreCategoria(producto.getCategoriaId());

        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .marca(producto.getMarca())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .categoriaId(producto.getCategoriaId())
                .categoriaNombre(categoriaNombre)
                .fechaCreacion(producto.getFechaCreacion())
                .build();
    }

    private String obtenerNombreCategoria(Integer categoriaId) {
        try {
            String sql = "SELECT nombre FROM categoria WHERE id = ?";
            return jdbcTemplate.queryForObject(sql, String.class, categoriaId);
        } catch (Exception e) {
            return "Sin categoría";
        }
    }
}
```

---

### PASO 9: Crear Controlador de Producto

**Archivo:** `src/main/java/com/web/prime_drip_club/controllers/ProductoController.java`

```java
package com.web.prime_drip_club.controllers;

import com.web.prime_drip_club.dto.Producto.ProductoRequest;
import com.web.prime_drip_club.dto.Producto.ProductoResponse;
import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<Response<List<ProductoResponse>>> obtenerTodos() {
        List<ProductoResponse> productos = productoService.obtenerTodos();
        Response<List<ProductoResponse>> response = Response.<List<ProductoResponse>>builder()
                .responseCode(200)
                .success(true)
                .message("Productos obtenidos exitosamente")
                .data(productos)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activos")
    public ResponseEntity<Response<List<ProductoResponse>>> obtenerActivos() {
        List<ProductoResponse> productos = productoService.obtenerActivos();
        Response<List<ProductoResponse>> response = Response.<List<ProductoResponse>>builder()
                .responseCode(200)
                .success(true)
                .message("Productos activos obtenidos exitosamente")
                .data(productos)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<ProductoResponse>> obtenerPorId(@PathVariable Integer id) {
        ProductoResponse producto = productoService.obtenerPorId(id);
        Response<ProductoResponse> response = Response.<ProductoResponse>builder()
                .responseCode(200)
                .success(true)
                .message("Producto obtenido exitosamente")
                .data(producto)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Response<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse producto = productoService.crear(request);
        Response<ProductoResponse> response = Response.<ProductoResponse>builder()
                .responseCode(201)
                .success(true)
                .message("Producto creado exitosamente")
                .data(producto)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<ProductoResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequest request) {
        ProductoResponse producto = productoService.actualizar(id, request);
        Response<ProductoResponse> response = Response.<ProductoResponse>builder()
                .responseCode(200)
                .success(true)
                .message("Producto actualizado exitosamente")
                .data(producto)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> eliminar(@PathVariable Integer id) {
        productoService.eliminar(id);
        Response<Void> response = Response.<Void>builder()
                .responseCode(204)
                .success(true)
                .message("Producto eliminado exitosamente")
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }
}
```

---

## 🔄 Flujo Completo en Acción

```
1. Usuario selecciona imagen en el frontend
   ↓
2. Frontend llama a POST /v1/upload/product-image
   ↓
3. FileStorageService guarda en /uploads/images/
   ↓
4. Backend retorna: { imageUrl: "/uploads/images/1234.jpg" }
   ↓
5. Usuario completa formulario con URL de imagen
   ↓
6. Frontend llama a POST /v1/productos
   ↓
7. ProductoService guarda en base de datos
   ↓
8. Backend retorna producto creado con categoriaNombre
   ↓
9. Frontend muestra lista de productos actualizada
```

---

## 🧪 Testing Manual

### 1. Probar Upload de Imagen

```bash
curl -X POST http://localhost:8080/v1/upload/product-image \
  -F "image=@/ruta/a/imagen.jpg"

# Respuesta esperada:
{
  "responseCode": 200,
  "success": true,
  "message": "Imagen subida exitosamente",
  "data": {
    "imageUrl": "/uploads/images/1738541234567-abc123.jpg"
  }
}
```

### 2. Probar Crear Producto

```bash
curl -X POST http://localhost:8080/v1/productos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TU_TOKEN_JWT" \
  -d '{
    "nombre": "Reloj Premium",
    "descripcion": "Reloj de lujo para caballero",
    "precio": 50000000,
    "stock": 10,
    "marca": "Rolex",
    "imagenUrl": "/uploads/images/1738541234567-abc123.jpg",
    "activo": true,
    "categoriaId": 1
  }'
```

### 3. Probar Obtener Todos los Productos

```bash
curl -X GET http://localhost:8080/v1/productos \
  -H "Authorization: Bearer TU_TOKEN_JWT"

# Los productos vendrán con categoriaNombre incluido
```

---

## ✅ Checklist de Implementación

### Backend

- [ ] Configurar `application.properties` con propiedades de multipart
- [ ] Crear `FileStorageService`
- [ ] Crear `FileUploadController`
- [ ] Actualizar `CorsConfig` para servir archivos estáticos
- [ ] Crear DTOs: `ProductoRequest` y `ProductoResponse`
- [ ] Crear interfaz `ProductoRepository`
- [ ] Implementar `ProductoRepositoryImpl`
- [ ] Crear `ProductoService` (incluye obtención de categoriaNombre)
- [ ] Crear `ProductoController`
- [ ] Probar endpoint de upload: `POST /v1/upload/product-image`
- [ ] Probar CRUD de productos

### Frontend

- [ ] Crear servicio para upload de imágenes
- [ ] Crear servicio para CRUD de productos
- [ ] Implementar formulario de producto con upload
- [ ] Mostrar productos con nombre de categoría
- [ ] Implementar edición de productos
- [ ] Implementar eliminación de productos

---

## 📊 Arquitectura de Datos

### Modelo Producto

```java
@Data
@Builder
public class Producto {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String marca;
    private String imagenUrl;         // URL relativa de la imagen
    private Boolean activo;
    private Integer categoriaId;      // FK a tabla categoria
    private LocalDateTime fechaCreacion;
}
```

### DTO Response con Categoría

```java
@Data
@Builder
public class ProductoResponse {
    private Integer id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String marca;
    private String imagenUrl;
    private Boolean activo;
    private Integer categoriaId;
    private String categoriaNombre;   // ⭐ Nombre de la categoría para el frontend
    private LocalDateTime fechaCreacion;
}
```

---

## 🔒 Validaciones Implementadas

### En FileStorageService:

- ✅ Validar que sea archivo de imagen (content-type)
- ✅ Validar tamaño máximo (5MB)
- ✅ Generar nombres únicos (timestamp + UUID)
- ✅ Crear directorio si no existe

### En ProductoRequest:

- ✅ Nombre obligatorio (máx 150 caracteres)
- ✅ Descripción opcional (máx 1000 caracteres)
- ✅ Precio obligatorio y mayor a 0
- ✅ Stock obligatorio y no negativo
- ✅ Marca obligatoria (máx 100 caracteres)
- ✅ ImagenUrl obligatoria (máx 255 caracteres)
- ✅ Estado activo obligatorio
- ✅ CategoriaId obligatorio

---

## 🐛 Troubleshooting

### Error: "No se puede crear directorio"

```bash
chmod 755 uploads/
chmod 755 uploads/images/
```

### Error: "CORS policy"

Verificar que CorsConfig esté configurado correctamente con los origins permitidos.

### Error: "File size limit exceeded"

```properties
# Aumentar límite en application.properties
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### Error: "La imagen no se muestra"

- Verificar que el directorio `uploads/images/` existe
- Verificar que CorsConfig tiene la configuración de ResourceHandler
- Verificar permisos del directorio

---

## 📝 Notas Adicionales

- Las imágenes se guardan con timestamp + UUID para evitar colisiones
- Al eliminar un producto, su imagen también se elimina del servidor
- Al actualizar un producto con nueva imagen, la antigua se elimina automáticamente
- El directorio `uploads/images/` se crea automáticamente si no existe
- Tamaño máximo de imagen: 5MB (configurable)
- Formatos permitidos: JPG, JPEG, PNG, GIF, WEBP
- El campo `categoriaNombre` se incluye en todas las respuestas de productos

---

## 🚀 Endpoints Disponibles

### Upload

- `POST /v1/upload/product-image` - Subir imagen de producto

### Productos

- `GET /v1/productos` - Obtener todos los productos (con categoriaNombre)
- `GET /v1/productos/activos` - Obtener productos activos
- `GET /v1/productos/{id}` - Obtener producto por ID
- `POST /v1/productos` - Crear producto
- `PUT /v1/productos/{id}` - Actualizar producto
- `DELETE /v1/productos/{id}` - Eliminar producto

---

**Última actualización:** Febrero 2, 2026
**Proyecto:** Prime Drip Club
**Tecnologías:** Spring Boot 3.x + JDBC Template + MySQL + Flyway
