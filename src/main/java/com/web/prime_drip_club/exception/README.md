# Sistema de Manejo de Excepciones

Este directorio contiene el sistema centralizado de manejo de excepciones de la aplicación.

## 📋 Componentes

### 1. GlobalExceptionHandler

Clase principal que intercepta y maneja todas las excepciones de la aplicación usando `@RestControllerAdvice`.

**Características:**

- Captura excepciones en toda la aplicación
- Convierte excepciones en respuestas HTTP estructuradas
- Usa `ErrorResponse` para formato consistente

### 2. ErrorResponse

DTO que define el formato estándar de respuesta de error:

```json
{
  "timestamp": "2026-01-26T10:30:00",
  "status": 500,
  "error": "Database Error",
  "message": "Error al conectar con la base de datos",
  "path": "/api/usuarios"
}
```

### 3. Excepciones Personalizadas

- **DatabaseException**: Errores relacionados con la base de datos

## 🚀 Cómo Crear una Nueva Excepción Personalizada

### Paso 1: Crear la clase de excepción

```java
package com.web.prime_drip_club.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### Paso 2: Agregar el manejador en GlobalExceptionHandler

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
        ResourceNotFoundException ex,
        WebRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Resource Not Found")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
}
```

### Paso 3: Usar en tus servicios

```java
@Service
public class UsuarioService {

    public Usuario obtenerUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id);

        if (usuario == null) {
            throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
        }

        return usuario;
    }
}
```

## 📦 Ejemplos de Excepciones Comunes

### ValidationException

Para errores de validación de datos:

```java
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
```

**Uso:**

```java
if (email == null || email.isEmpty()) {
    throw new ValidationException("El email es requerido");
}
```

**Manejador:**

```java
@ExceptionHandler(ValidationException.class)
public ResponseEntity<ErrorResponse> handleValidationException(
        ValidationException ex, WebRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
}
```

### UnauthorizedException

Para errores de autenticación:

```java
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

**Uso:**

```java
if (!passwordEncoder.matches(password, usuario.getPassword())) {
    throw new UnauthorizedException("Credenciales inválidas");
}
```

**Manejador:**

```java
@ExceptionHandler(UnauthorizedException.class)
public ResponseEntity<ErrorResponse> handleUnauthorizedException(
        UnauthorizedException ex, WebRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
}
```

### DuplicateResourceException

Para recursos duplicados:

```java
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

**Uso:**

```java
if (usuarioRepository.existsByEmail(email)) {
    throw new DuplicateResourceException("Ya existe un usuario con el email: " + email);
}
```

**Manejador:**

```java
@ExceptionHandler(DuplicateResourceException.class)
public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
        DuplicateResourceException ex, WebRequest request) {

    ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.CONFLICT.value())
            .error("Duplicate Resource")
            .message(ex.getMessage())
            .path(request.getDescription(false).replace("uri=", ""))
            .build();

    return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
}
```

## 🔄 Flujo de Ejecución

```
[Controller/Service]
      ↓
[Lanza excepción personalizada]
      ↓
[GlobalExceptionHandler intercepta]
      ↓
[Busca @ExceptionHandler correspondiente]
      ↓
[Crea ErrorResponse]
      ↓
[Retorna ResponseEntity con status HTTP]
      ↓
[Cliente recibe JSON estructurado]
```

## 🎯 Códigos HTTP Recomendados

| Código | Uso                   | Ejemplo                          |
| ------ | --------------------- | -------------------------------- |
| 400    | Bad Request           | Datos de entrada inválidos       |
| 401    | Unauthorized          | Credenciales incorrectas         |
| 403    | Forbidden             | Sin permisos para acceder        |
| 404    | Not Found             | Recurso no encontrado            |
| 409    | Conflict              | Recurso duplicado                |
| 500    | Internal Server Error | Error del servidor/base de datos |

## 💡 Buenas Prácticas

1. **Nombres descriptivos**: Usa nombres claros para tus excepciones (`ResourceNotFoundException` en lugar de `NotFoundException`)

2. **Mensajes informativos**: Proporciona contexto en el mensaje

   ```java
   throw new ResourceNotFoundException("Usuario no encontrado con ID: " + id);
   ```

3. **Un manejador por excepción**: Cada excepción personalizada debe tener su propio `@ExceptionHandler`

4. **Status HTTP apropiado**: Usa el código HTTP correcto para cada tipo de error

5. **Manejador genérico**: Mantén siempre un manejador para `Exception.class` como fallback

6. **No exponer información sensible**: Evita mostrar stack traces o detalles internos al cliente
