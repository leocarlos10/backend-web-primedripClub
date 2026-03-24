# Plan: Checkout y Pago Wompi MVP

## Resumen Ejecutivo

Implementación de un flujo transaccional end-to-end en backend para convertir carritos en pedidos oficiales y procesar pagos con **Wompi** como pasarela de pagos.

### Objetivo
- Permitir que usuarios **anónimos interactúen libremente con carrito**, pero **requieren registro obligatorio al crear pedido**.
- Procesar pagos de forma segura con Wompi solo para usuarios logueados.
- Mantener un único modelo de pedido/pago (sin doble lógica invitado/registrado).
- Usar **webhook de Wompi como fuente de verdad** para confirmación de pagos.

### Alcance MVP
✅ **Incluido:**
- Carrito público y libre para anónimos (con `sessionId`)
- Flujo carrito → registro → pedido (transaccional)
- Merge/fusión de carrito anónimo → carrito de usuario registrado
- Integración Wompi (PSE, tarjeta crédito/débito, Nequi) solo para logueados
- Webhook como confirmación final de pago
- Modelo único de pedido/pago (siempre con `usuario_id`)
- Validación de stock en checkout
- Persistencia segura de transacciones
- Pruebas base (unitarias e integración)

❌ **Fuera de alcance MVP:**
- Flujo guest checkout (pedido sin registro)
- Antifraude avanzado
- Conciliación contable automática
- Panel administrativo de pagos
- Soporte multimoneda
- Reembolsos parciales o automáticos

---

## Decisiones Clave

| Decisión | Opción Seleccionada | Razón |
|----------|:------------------:|-------|
| **Fuente de pedido** | Siempre desde carrito del usuario logueado | Evita manipulación de precios, integridad de inventory |
| **Checkout** | Requiere registro obligatorio | Un modelo único, sin doble lógica guest/registrado |
| **Merge carrito** | Anónimo → Logueado post-autenticación | Preserva ítems, suma cantidades por producto |
| **Métodos de pago MVP** | PSE, Tarjeta, Nequi | Mayor cobertura para Colombia |
| **Confirmación de pago** | Webhook (fuente de verdad) | Respuesta inmediata para UX; estado final = webhook |
| **Endpoint principal** | `POST /v1/pedidos/checkout` | Requiere JWT |
| **Arquitectura de acceso** | JWT obligatorio para pedido/pago | Trazabilidad clara, sin complejidad dual |

---

## Fases de Implementación

### Fase 0: Merge de Carrito Anónimo → Logueado (Pre-requisito)

**Objetivo:** Permitir que un usuario anónimo que se registra mantenga sus ítems en el carrito.

#### Paso 0.1: Crear método de fusión en CarritoService

**En [CarritoService.java](src/main/java/com/web/prime_drip_club/service/CarritoService.java):**
Agregar método (será invocado por AuthService después de login/registro exitoso):

```java
@Transactional
public void fusionarCarritoAnonimoAlUsuarioRegistrado(
        String sessionIdAnonimoAnterior, 
        Long usuarioIdNuevo) {
    
    // 1. Obtener carrito anónimo
    CarritoResponse carritoAnonimoResponse = carritoRespository
        .obtenerCarrito(null, null, sessionIdAnonimoAnterior)
        .orElse(null);
    
    if (carritoAnonimoResponse == null || carritoAnonimoResponse.getItems().isEmpty()) {
        // No hay carrito anónimo o está vacío, crear uno nuevo vinculado a usuario
        crearCarritoParaUsuario(usuarioIdNuevo);
        return;
    }
    
    // 2. Crear/obtener carrito del usuario registrado
    Long carritoAnonimoId = carritoAnonimoResponse.getCarritoId();
    Long carritoUsuarioId;
    
    Optional<Long> carritoUsuarioOpt = carritoRespository.obtenerCarritoId(usuarioIdNuevo);
    if (carritoUsuarioOpt.isEmpty()) {
        // Crear nuevo carrito para el usuario
        CarritoRequest req = CarritoRequest.builder()
            .usuarioId(usuarioIdNuevo)
            .build();
        carritoUsuarioId = carritoRespository.guardarCarrito(req);
    } else {
        carritoUsuarioId = carritoUsuarioOpt.get();
    }
    
    // 3. Copiar todos los ítems del carrito anónimo al del usuario
    // (detalleCarritoRepository usará ON DUPLICATE KEY UPDATE para sumar cantidades)
    for (DetalleCarritoResponse itemAnon : carritoAnonimoResponse.getItems()) {
        DetalleCarritoRequest fusionado = DetalleCarritoRequest.builder()
            .carritoId(carritoUsuarioId)
            .productoId(itemAnon.getProductoId())
            .cantidad(itemAnon.getCantidad())
            .precioUnitario(itemAnon.getPrecioUnitario())
            .build();
        
        detalleCarritoRepository.guardarDetalleCarrito(fusionado);
    }
    
    // 4. Eliminar todos los detalles del carrito anónimo
    carritoRespository.limpiarCarritoDetalle(carritoAnonimoId);
    
    // 5. Eliminar el carrito anónimo mismo
    carritoRespository.eliminarCarrito(carritoAnonimoId);
}

private void crearCarritoParaUsuario(Long usuarioId) {
    CarritoRequest req = CarritoRequest.builder()
        .usuarioId(usuarioId)
        .build();
    carritoRespository.guardarCarrito(req);
}
```

**En [AuthController.java](src/main/java/com/web/prime_drip_club/controllers/AuthController.java):**
Después de registro o login exitoso, el frontend envía el `sessionId` anterior (si hay) para fusionar:

```java
// Después de autenticación exitosa
if (request.getSessionIdAnterior() != null) {
    carritoService.fusionarCarritoAnonimoAlUsuarioRegistrado(
        request.getSessionIdAnterior(),
        usuarioRegistrado.getId()
    );
}
```

---

### Fase 1: Consolidar Contrato de Checkout (Solo Registrados)

**Objetivo:** Definir contratos de API para checkout de usuarios **autenticados**. Checkout requiere JWT.

#### Paso 1.1: Crear DTO de Checkout

**Archivo:** `src/main/java/com/web/prime_drip_club/dto/pedido/CheckoutRequest.java` (NUEVO)

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutRequest {
    
    @NotNull(message = "El ID del carrito es obligatorio")
    private Long carritoId;

    // Usuario logueado se obtiene del JWT (Spring Security)
    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String emailContacto;

    @NotBlank(message = "El nombre de contacto es obligatorio")
    @Size(min = 2, max = 150)
    private String nombreContacto;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 20)
    private String telefono;

    @NotNull(message = "La dirección es obligatoria")
    private DireccionCheckoutRequest direccion;

    private String notas;
}
```

**Archivo:** `src/main/java/com/web/prime_drip_club/dto/pedido/DireccionCheckoutRequest.java` (NUEVO)

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DireccionCheckoutRequest {
    
    @NotBlank(message = "La dirección completa es obligatoria")
    private String direccionCompleta;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    private String ciudad;

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100)
    private String departamento;

    @Size(max = 10)
    private String codigoPostal;

    @Size(max = 20)
    private String telefonoContacto;
}
```

**Archivo:** `src/main/java/com/web/prime_drip_club/dto/pedido/CheckoutResponse.java` (NUEVO)

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutResponse {
    
    private Long pedidoId;
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private BigDecimal total;
    private EstadoPedido estado;  // Inicial: PAGO_PENDIENTE
    private LocalDateTime fechaCreacion;
}
```

#### Paso 1.2: Ajustar Reglas de Seguridad

**Archivo:** [src/main/java/com/web/prime_drip_club/config/security/WebSecurityConfig.java](src/main/java/com/web/prime_drip_club/config/security/WebSecurityConfig.java)

NO cambiar nada. El estado actual ya protege `/v1/pedidos/**` y `/v1/pagos/**` con `authenticated()`. Solo permitir público al webhook:

```java
.requestMatchers("POST", "/v1/pagos/webhook/**").permitAll()
.requestMatchers("POST", "/v1/pedidos/checkout").authenticated()  // Requiere JWT
.requestMatchers("GET", "/v1/pedidos/**").authenticated()         // Requiere JWT
.requestMatchers("POST", "/v1/pagos/iniciar").authenticated()      // Requiere JWT
.requestMatchers("GET", "/v1/pagos/estado").authenticated()        // Requiere JWT
```

**Nota:** Los endpoints de carrito y autenticación permanecen públicos para permitir flujo de invitados hasta checkout.

---

### Fase 2: Implementar Creación de Pedido Transaccional

**Objetivo:** Levantar la cadena de repositories, servicios y controladores para crear pedido desde carrito.

#### Paso 2.1: Crear PedidoRepository

**Archivo:** `src/main/java/com/web/prime_drip_club/repository/PedidoRepository.java` (NUEVO)

```java
public interface PedidoRepository {
    
    /**
     * Obtiene los datos completos del carrito asociado a usuarioId o sessionId.
     * Incluye ítems, precios unitarios y referencias de productos.
     */
    Optional<CarritoConDetalles> obtenerCarritoParaCheckout(Long carritoId, Long usuarioId, String sessionId);
    
    /**
     * Crea un nuevo pedido en estado PAGO_PENDIENTE.
     * Retorna el ID generado del pedido.
     */
    Long crearPedido(Pedido pedido);
    
    /**
     * Inserta los ítems del pedido (detalle_pedido) en batch.
     * Validar que los precios vienen del carrito original (no manipulados).
     */
    void crearDetallesPedido(Long pedidoId, List<DetallePedidoParaInsertar> detalles);
    
    /**
     * Limpia todos los ítems del carrito tras éxito en crear pedido.
     */
    void limpiarCarrito(Long carritoId);
    
    /**
     * Obtiene un pedido por ID con sus ítems completos.
     */
    Optional<Pedido> obtenerPedidoConDetalles(Long pedidoId);
    
    /**
     * Actualiza el estado del pedido (ej: PAGADO, CANCELADO, etc).
     */
    Boolean actualizarEstadoPedido(Long pedidoId, EstadoPedido estado);
}
```

**Archivo:** `src/main/java/com/web/prime_drip_club/repository/impl/PedidoRepositoryImpl.java` (NUEVO)

Implementación JDBC con énfasis en:
- Lectura de carrito validando ownership (`usuarioId` O `sessionId`).
- Inserción atómica: pedido + detalles en una transacción (debe usar `@Transactional` en servicio).
- Lectura de precios unitarios desde `detalle_carrito` para evitar que cliente manipule.
- Limpieza de carrito tras éxito.

#### Paso 2.2: Crear PedidoService

**Archivo:** `src/main/java/com/web/prime_drip_club/service/PedidoService.java` (NUEVO)

```java
@Service
@RequiredArgsConstructor
public class PedidoService {
    
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    
    @Transactional
    public CheckoutResponse crearPedidoDesdeCarrito(
            CheckoutRequest request,
            Long usuarioIdDelJwt) {  // Inyectado desde el JWT
        
        // 1. Validar propiedad del carrito (solo del usuario logueado)
        CarritoConDetalles carrito = pedidoRepository
            .obtenerCarritoParaCheckout(request.getCarritoId(), usuarioIdDelJwt, null)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Carrito no encontrado o no pertenece al usuario logueado"));
        
        // 2. Validar que el carrito tiene ítems
        if (carrito.getDetalles().isEmpty()) {
            throw new ValidationException("El carrito está vacío. No se puede crear un pedido.");
        }
        
        // 3. Validar stock disponible para cada producto
        for (DetalleCarritoResponse detalle : carrito.getDetalles()) {
            Producto producto = productoRepository.findById(detalle.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + detalle.getProductoId()));
            
            if (producto.getStock() < detalle.getCantidad()) {
                throw new ValidationException(
                    "Stock insuficiente para producto '" + producto.getNombre() + "'. " +
                    "Disponible: " + producto.getStock() + ", solicitado: " + detalle.getCantidad()
                );
            }
        }
        
        // 4. Calcular totales
        BigDecimal subtotal = carrito.getDetalles().stream()
            .map(d -> d.getPrecioUnitario().multiply(BigDecimal.valueOf(d.getCantidad())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal costoEnvio = calcularCostoEnvio(request.getDireccion()); // Ver nota abajo
        BigDecimal total = subtotal.add(costoEnvio);
        
        // 5. Crear instancia de Pedido (con usuarioId ya validado del JWT)
        Pedido pedido = Pedido.builder()
            .usuarioId(usuarioIdDelJwt)  // Siempre logueado
            .emailContacto(request.getEmailContacto())
            .nombreContacto(request.getNombreContacto())
            .telefono(request.getTelefono())
            .estado(EstadoPedido.PAGO_PENDIENTE)
            .subtotal(subtotal)
            .costoEnvio(costoEnvio)
            .total(total)
            .notas(request.getNotas())
            .direccionEnvioSnapshot(convertirASnapshot(request.getDireccion()))
            .build();
        
        // 6. Persistir pedido (genera ID)
        Long pedidoId = pedidoRepository.crearPedido(pedido);
        pedido.setId(pedidoId);
        
        // 7. Persistir detalles del pedido (con precios originales del carrito)
        List<DetallePedidoParaInsertar> detalles = carrito.getDetalles().stream()
            .map(d -> DetallePedidoParaInsertar.builder()
                .pedidoId(pedidoId)
                .productoId(d.getProductoId())
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .build())
            .toList();
        
        pedidoRepository.crearDetallesPedido(pedidoId, detalles);
        
        // 8. Limpiar carrito (eliminar detalle_carrito)
        pedidoRepository.limpiarCarrito(request.getCarritoId());
        
        // 9. Retornar respuesta
        return CheckoutResponse.builder()
            .pedidoId(pedidoId)
            .subtotal(subtotal)
            .costoEnvio(costoEnvio)
            .total(total)
            .estado(EstadoPedido.PAGO_PENDIENTE)
            .fechaCreacion(LocalDateTime.now())
            .build();
    }
    
    private BigDecimal calcularCostoEnvio(DireccionCheckoutRequest direccion) {
        return new BigDecimal("5000.00");
    }
    
    private String convertirASnapshot(DireccionCheckoutRequest direccion) {
        try {
            return new ObjectMapper().writeValueAsString(direccion);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar dirección", e);
        }
    }
}
```

**Nota sobre `calcularCostoEnvio`:**
Para MVP, se asume una tarifa fija de $5.000 COP. En producción, se puede:
- Consultar una tabla `tarifa_envio` por ciudad/departamento.
- Integrar API de envío externo (Servientrega, etc).
- Definir rangos de peso/volumen.

Por ahora, defínelo como constante en `application.properties`:
```properties
shipping.cost.mvp=5000.00
```

#### Paso 2.3: Crear PedidoController

**Archivo:** `src/main/java/com/web/prime_drip_club/controllers/PedidoController.java` (NUEVO)

```java
@RestController
@RequestMapping("/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {
    
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    
    /**
     * Endpoint de checkout: convierte carrito en pedido
     * Acceso: Público (validada por CheckoutRequest)
     */
    @PostMapping("/checkout")
    public ResponseEntity<Response<CheckoutResponse>> checkout(
            @Valid @RequestBody CheckoutRequest request) {
        
        try {
            CheckoutResponse response = pedidoService.crearPedidoDesdeCarrito(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(
                Response.<CheckoutResponse>builder()
                    .responseCode(201)
                    .success(true)
                    .data(response)
                    .message("Pedido creado exitosamente. Por favor proceda al pago.")
                    .build()
            );
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(
                Response.<CheckoutResponse>builder()
                    .responseCode(400)
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Response.<CheckoutResponse>builder()
                    .responseCode(404)
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Endpoint de consulta: obtener estado del pedido
     * Requiere JWT de usuario logueado
     */
    @GetMapping("/{pedidoId}")
    public ResponseEntity<Response<PedidoResponse>> obtenerPedido(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal UserDetailsImpl user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Response.<PedidoResponse>builder()
                    .responseCode(401)
                    .success(false)
                    .message("Se requiere autenticación")
                    .build()
            );
        }
        
        Pedido pedido = pedidoRepository.obtenerPedidoConDetalles(pedidoId)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        
        // Validar que el pedido pertenece al usuario logueado
        if (!pedido.getUsuarioId().equals(user.getUsuario().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                Response.<PedidoResponse>builder()
                    .responseCode(403)
                    .success(false)
                    .message("No tiene permiso para acceder a este pedido")
                    .build()
            );
        }
        
        return ResponseEntity.ok(
            Response.<PedidoResponse>builder()
                .responseCode(200)
                .success(true)
                .data(mapearAPedidoResponse(pedido))
                .build()
        );
    }
    
    private PedidoResponse mapearAPedidoResponse(Pedido pedido) {
        return PedidoResponse.builder()
            .id(pedido.getId())
            .usuarioId(pedido.getUsuarioId())
            .emailContacto(pedido.getEmailContacto())
            .nombreContacto(pedido.getNombreContacto())
            .telefono(pedido.getTelefono())
            .subtotal(pedido.getSubtotal())
            .costoEnvio(pedido.getCostoEnvio())
            .total(pedido.getTotal())
            .estado(pedido.getEstado())
            .notas(pedido.getNotas())
            .direccionEnvioSnapshot(pedido.getDireccionEnvioSnapshot())
            .fechaCreacion(pedido.getFechaCreacion())
            .build();
    }
}
```

---

### Fase 3: Preparar Persistencia para Wompi

**Objetivo:** Extender modelo de `pago` con campos de tracking de pasarela.

#### Paso 3.1: Crear Migración V16

**Archivo:** `src/main/resources/db/migration/V16__alter_pago_add_wompi_tracking_fields.sql` (NUEVO)

```sql
-- ============================================
-- V16: Ampliar tabla PAGO con tracking Wompi
-- ============================================

ALTER TABLE pago
ADD COLUMN provider VARCHAR(50) COMMENT 'Proveedor de pago (ej: WOMPI)',
ADD COLUMN provider_transaction_id VARCHAR(100) UNIQUE COMMENT 'ID transacción en Wompi',
ADD COLUMN reference_code VARCHAR(100) UNIQUE COMMENT 'Código de referencia de Wompi',
ADD COLUMN idempotency_key VARCHAR(255) UNIQUE COMMENT 'Clave de idempotencia para reintentos',
ADD COLUMN webhook_status ENUM('PENDING', 'VERIFIED', 'FAILED', 'EXPIRED') DEFAULT 'PENDING' 
    COMMENT 'Estado según webhook recibido',
ADD COLUMN webhook_payload LONGTEXT COMMENT 'Payload JSON completo del webhook',
ADD COLUMN fecha_creacion_intento TIMESTAMP DEFAULT CURRENT_TIMESTAMP 
    COMMENT 'Cuándo se creó el intento de pago',
ADD COLUMN fecha_confirmacion TIMESTAMP NULL 
    COMMENT 'Cuándo se confirmó el pago (webhook o timeout)',
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Índices para búsquedas críticas
CREATE UNIQUE INDEX idx_pago_provider_transaction ON pago(provider_transaction_id);
CREATE UNIQUE INDEX idx_pago_reference_code ON pago(reference_code);
CREATE UNIQUE INDEX idx_pago_idempotency ON pago(idempotency_key);
CREATE INDEX idx_pago_webhook_status ON pago(webhook_status);
CREATE INDEX idx_pago_pedido_estado ON pago(pedido_id, estado);
```

#### Paso 3.2: Actualizar Modelo Pago

**Archivo:** [src/main/java/com/web/prime_drip_club/models/Pago.java](src/main/java/com/web/prime_drip_club/models/Pago.java)

Agregar campos nuevos:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {
    private Long id;
    private Long pedidoId;
    private MetodoPago metodo;
    private EstadoPago estado;
    private String referencia;
    private String mensajeError;
    private String metadata;
    private LocalDateTime fechaPago;
    
    // NUEVOS CAMPOS WOMPI
    private String provider;                    // "WOMPI"
    private String providerTransactionId;       // ID de Wompi
    private String referenceCode;               // Referencia de Wompi
    private String idempotencyKey;              // Clave de idempotencia
    private String webhookStatus;               // PENDING, VERIFIED, FAILED, EXPIRED
    private String webhookPayload;              // JSON crudo
    private LocalDateTime fechaCreacionIntento; // Cuándo se inició
    private LocalDateTime fechaConfirmacion;    // Cuándo se confirmó
    private LocalDateTime updatedAt;
}
```

#### Paso 3.3: Crear PagoRepository

**Archivo:** `src/main/java/com/web/prime_drip_club/repository/PagoRepository.java` (NUEVO)

```java
public interface PagoRepository {
    
    /**
     * Crea un nuevo intento de pago en estado PENDIENTE.
     */
    Long crearPago(Pago pago);
    
    /**
     * Obtiene un pago por ID con sus detalles completos.
     */
    Optional<Pago> obtenerPorId(Long pagoId);
    
    /**
     * Obtiene el pago asociado a un pedido (usualmente 1:1).
     */
    Optional<Pago> obtenerPorPedidoId(Long pedidoId);
    
    /**
     * Obtiene un pago por su referencia Wompi.
     */
    Optional<Pago> obtenerPorReferenceCode(String referenceCode);
    
    /**
     * Obtiene un pago por su transaction ID de Wompi.
     */
    Optional<Pago> obtenerPorProviderTransactionId(String transactionId);
    
    /**
     * Actualiza datos post-webhook: estado, webhook_payload, webhook_status, fecha_confirmacion.
     */
    Boolean actualizarPorWebhook(Long pagoId, EstadoPago estado, String webhookStatus, 
                                   String webhookPayload, LocalDateTime fechaConfirmacion);
    
    /**
     * Registra un error en la transacción.
     */
    Boolean actualizarError(Long pagoId, String mensajeError, String webhookStatus);
}
```

**Archivo:** `src/main/java/com/web/prime_drip_club/repository/impl/PagoRepositoryImpl.java` (NUEVO)

Implementación JDBC con énfasis en:
- Inserción con campos iniciales: `provider=WOMPI`, `estado=PENDIENTE`, `webhook_status=PENDING`.
- Actualización segura por webhook: usar referencias de Wompi como clave de búsqueda.
- Manejo de duplicados idempotentes: validar `idempotency_key` antes de insertar.

---

### Fase 4: Integración API Wompi

**Objetivo:** Implementar cliente HTTP hacia Wompi y endpoints de iniciación/webhook.

#### Paso 4.1: Configurar Wompi en application.properties

**Archivo:** [src/main/resources/application.properties](src/main/resources/application.properties)

Agregar:

```properties
# ============================================
# WOMPI Configuration (MVP)
# ============================================
wompi.enabled=true
wompi.api.base-url=${WOMPI_API_BASE_URL:https://api.wompi.co/v1}
wompi.api.public-key=${WOMPI_PUBLIC_KEY}
wompi.api.private-key=${WOMPI_PRIVATE_KEY}
wompi.api.integrity-secret=${WOMPI_INTEGRITY_SECRET}
wompi.webhook.url=${WOMPI_WEBHOOK_URL:http://localhost:8080/v1/pagos/webhook/wompi}
wompi.redirect.url=${WOMPI_REDIRECT_URL:https://tudominio.com/orden-confirmacion}
wompi.payment.methods=PSE,TARJETA_CREDITO,TARJETA_DEBITO,NEQUI
wompi.currency=COP
wompi.timeout.ms=30000
```

**Nota:** Las variables con `${}` deben estar disponibles en entorno (variables de entorno o archivo `.env` si usas Spring Cloud Config).

#### Paso 4.2: Crear Cliente HTTP Wompi

**Archivo:** `src/main/java/com/web/prime_drip_club/integration/wompi/WompiClient.java` (NUEVO)

```java
@Component
@RequiredArgsConstructor
public class WompiClient {
    
    private final RestTemplate restTemplate;
    private final WompiConfig wompiConfig;
    private static final Logger logger = LoggerFactory.getLogger(WompiClient.class);
    
    /**
     * Crea una transacción de pago en Wompi.
     * Retorna datos necesarios para continuar el flujo de pago (URL redirect o datos de autenticación).
     */
    public WompiCreateTransactionResponse crearTransaccion(WompiCreateTransactionRequest request) 
            throws WompiException {
        
        String url = wompiConfig.getApiBaseUrl() + "/transactions";
        
        // Agregar metadata obligatoria
        request.setAcceptanceToken(wompiConfig.getPublicKey());
        
        try {
            logger.info("Creando transacción en Wompi: pedidoId={}, monto={}", 
                request.getReference(), request.getAmountInCents());
            
            HttpHeaders headers = crearHeaders();
            HttpEntity<WompiCreateTransactionRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<WompiTransactionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                WompiTransactionResponse.class
            );
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new WompiException("Respuesta inválida de Wompi: " + response.getStatusCode());
            }
            
            WompiTransaction transaction = response.getBody().getData();
            logger.info("Transacción creada: id={}, reference={}", 
                transaction.getId(), transaction.getReference());
            
            return WompiCreateTransactionResponse.builder()
                .transactionId(transaction.getId())
                .reference(transaction.getReference())
                .amountInCents(transaction.getAmountInCents())
                .paymentMethod(transaction.getPaymentMethod())
                .redirectUrl(transaction.getRedirectUrl())
                .status(transaction.getStatus())
                .build();
                
        } catch (RestClientException e) {
            logger.error("Error al crear transacción en Wompi", e);
            throw new WompiException("Error de conexión con Wompi: " + e.getMessage(), e);
        }
    }
    
    /**
     * Simula transacción aprobada (solo para testing/sandbox).
     * Wompi provee sandbox URL para esto.
     */
    public WompiCreateTransactionResponse aprobarTransaccionTest(String transactionId) 
            throws WompiException {
        // Implementación según documentación de Wompi Sandbox
        // Este endpoint es para tokens de prueba
        throw new UnsupportedOperationException("Ver documentación Wompi Sandbox");
    }
    
    /**
     * Valida la firma del webhook de Wompi.
     * Wompi envía header: X-Wompi-Signature = SHA256(payload + intimacy_secret)
     */
    public boolean validarWebhook(String payload, String signatureHeader) {
        try {
            String computed = computarSignatura(payload);
            return computed.equals(signatureHeader);
        } catch (Exception e) {
            logger.error("Error al validar firma del webhook", e);
            return false;
        }
    }
    
    private String computarSignatura(String payload) throws Exception {
        String secreto = wompiConfig.getIntegritySecret();
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secreto.getBytes(), "HmacSHA256"));
        byte[] hash = mac.doFinal(payload.getBytes());
        return Base64.getEncoder().encodeToString(hash);
    }
    
    private HttpHeaders crearHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Autenticación: Basic con llave privada
        String auth = wompiConfig.getPrivateKey() + ":";
        String base64Auth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + base64Auth);
        
        return headers;
    }
}
```

**Archivo:** `src/main/java/com/web/prime_drip_club/integration/wompi/WompiConfig.java` (NUEVO)

```java
@Configuration
@ConfigurationProperties(prefix = "wompi")
@Data
public class WompiConfig {
    
    private boolean enabled;
    
    @NestedConfigurationProperty
    private ApiConfig api = new ApiConfig();
    
    @NestedConfigurationProperty
    private WebhookConfig webhook = new WebhookConfig();
    
    @NestedConfigurationProperty
    private RedirectConfig redirect = new RedirectConfig();
    
    private String paymentMethods;
    private String currency;
    private long timeoutMs;
    
    // Getters para acceso simplificado
    public String getApiBaseUrl() { return api.baseUrl; }
    public String getPublicKey() { return api.publicKey; }
    public String getPrivateKey() { return api.privateKey; }
    public String getIntegritySecret() { return api.integritySecret; }
    public String getWebhookUrl() { return webhook.url; }
    public String getRedirectUrl() { return redirect.url; }
    
    @Data
    public static class ApiConfig {
        private String baseUrl;
        private String publicKey;
        private String privateKey;
        private String integritySecret;
    }
    
    @Data
    public static class WebhookConfig {
        private String url;
    }
    
    @Data
    public static class RedirectConfig {
        private String url;
    }
}
```

#### Paso 4.3: Crear PagoService

**Archivo:** `src/main/java/com/web/prime_drip_club/service/PagoService.java` (NUEVO)

```java
@Service
@RequiredArgsConstructor
public class PagoService {
    
    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;
    private final WompiClient wompiClient;
    private final WompiConfig wompiConfig;
    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);
    
    /**
     * Inicia un pago para un pedido.
     * Requiere que el usuario logueado sea propietario del pedido.
     * Retorna datos de continuación (URL redirect, datos autenticación, etc).
     */
    @Transactional
    public IniciarPagoResponse iniciarPago(IniciarPagoRequest request, Long usuarioIdLogueado) 
            throws WompiException {
        
        // 1. Validar pedido existe
        Pedido pedido = pedidoRepository.obtenerPedidoConDetalles(request.getPedidoId())
            .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
        
        // 2. Validar que el usuario logueado es propietario del pedido
        if (!pedido.getUsuarioId().equals(usuarioIdLogueado)) {
            throw new ValidationException("No tienes permiso para pagar este pedido");
        }
        
        // 3. Validar que el pedido está en estado correcto (PAGO_PENDIENTE)
        if (pedido.getEstado() != EstadoPedido.PAGO_PENDIENTE) {
            throw new ValidationException(
                "El pedido no está en estado de pago pendiente. Estado actual: " + pedido.getEstado()
            );
        }
        
        // 4. Validar que no existe pago en progreso
        Optional<Pago> pagoExistente = pagoRepository.obtenerPorPedidoId(request.getPedidoId());
        if (pagoExistente.isPresent() && 
            (pagoExistente.get().getEstado() == EstadoPago.PENDIENTE || 
             pagoExistente.get().getEstado() == EstadoPago.APROBADO)) {
            
            throw new ValidationException("Ya existe un pago en progreso para este pedido");
        }
        
        // 5. Crear intento de pago local
        String idempotencyKey = UUID.randomUUID().toString();
        Pago pago = Pago.builder()
            .pedidoId(request.getPedidoId())
            .metodo(request.getMetodo())
            .estado(EstadoPago.PENDIENTE)
            .provider("WOMPI")
            .idempotencyKey(idempotencyKey)
            .webhookStatus("PENDING")
            .fechaCreacionIntento(LocalDateTime.now())
            .build();
        
        Long pagoId = pagoRepository.crearPago(pago);
        pago.setId(pagoId);
        
        // 6. Crear transacción en Wompi
        try {
            WompiCreateTransactionRequest wompiRequest = WompiCreateTransactionRequest.builder()
                .reference(String.valueOf(request.getPedidoId()))
                .amountInCents(pedido.getTotal().multiply(BigDecimal.valueOf(100)).longValue())
                .currency(wompiConfig.getCurrency())
                .customerEmail(pedido.getEmailContacto())
                .paymentMethod(WompiPaymentMethod.builder()
                    .type(mapearAWompiType(request.getMetodo()))
                    .installments(1L)
                    .build())
                .redirectUrl(wompiConfig.getRedirectUrl() + "?pedidoId=" + request.getPedidoId())
                .idempotencyKey(idempotencyKey)
                .metadata(Map.of(
                    "pedido_id", String.valueOf(request.getPedidoId()),
                    "nombre_cliente", pedido.getNombreContacto(),
                    "timestamp", LocalDateTime.now().toString()
                ))
                .build();
            
            WompiCreateTransactionResponse wompiResponse = wompiClient.crearTransaccion(wompiRequest);
            
            // 7. Actualizar pago con datos de Wompi
            pago.setProviderTransactionId(wompiResponse.getTransactionId());
            pago.setReferenceCode(wompiResponse.getReference());
            
            pagoRepository.actualizarPorWebhook(
                pagoId,
                EstadoPago.PENDIENTE,
                "TRANSACCION_CREADA",
                null,
                null
            );
            
            // 8. Retornar respuesta con continuación
            return IniciarPagoResponse.builder()
                .pagoId(pagoId)
                .pedidoId(request.getPedidoId())
                .estado(EstadoPago.PENDIENTE)
                .metodo(request.getMetodo())
                .monto(pedido.getTotal())
                .referenceCode(wompiResponse.getReference())
                .redirectUrl(wompiResponse.getRedirectUrl())
                .mensaje("Pago iniciado. Por favor completa el pago en el siguiente paso.")
                .build();
            
        } catch (WompiException e) {
            logger.error("Error al crear transacción en Wompi para pedido: " + request.getPedidoId(), e);
            
            pagoRepository.actualizarError(pagoId, e.getMessage(), "WOMPI_ERROR");
            
            throw new RuntimeException("No se pudo iniciar el pago. Intenta nuevamente.", e);
        }
    }
    
    /**
     * Procesa webhooks recibidos de Wompi.
     * Actualiza estado del pago y del pedido según resultado.
     */
    @Transactional
    public void procesarWebhook(String payloadJson, String signature) 
            throws WompiException {
        
        // 1. Validar firma del webhook
        if (!wompiClient.validarWebhook(payloadJson, signature)) {
            throw new WompiException("Firma de webhook inválida");
        }
        
        // 2. Parsear payload
        WompiWebhookPayload webhook = parsePayload(payloadJson);
        logger.info("Procesando webhook Wompi: evento={}, referencia={}, estatus={}", 
            webhook.getEvent(), webhook.getTransaction().getReference(), webhook.getTransaction().getStatus());
        
        // 3. Resolver el pago por reference
        Pago pago = pagoRepository.obtenerPorReferenceCode(webhook.getTransaction().getReference())
            .orElseThrow(() -> new WompiException("Pago no encontrado para referencia: " + 
                webhook.getTransaction().getReference()));
        
        // 4. Validar idempotencia: si ya procesado, ignorar
        if (pago.getFechaConfirmacion() != null) {
            logger.warn("Webhook duplicado para pago: {}", pago.getId());
            return; // Idempotente
        }
        
        // 5. Actualizar estado según transacción
        EstadoPago estadoPago = mapearEstadoWompi(webhook.getTransaction().getStatus());
        String webhookStatus = webhook.getTransaction().getStatus();
        
        pagoRepository.actualizarPorWebhook(
            pago.getId(),
            estadoPago,
            webhookStatus,
            payloadJson,
            LocalDateTime.now()
        );
        
        // 6. Actualizar estado del pedido
        EstadoPedido estadoPedido;
        if (estadoPago == EstadoPago.APROBADO) {
            estadoPedido = EstadoPedido.PAGADO;
        } else if (estadoPago == EstadoPago.RECHAZADO || estadoPago == EstadoPago.CANCELADO) {
            estadoPedido = EstadoPedido.PAGO_PENDIENTE; // Permite reintentar
        } else {
            estadoPedido = EstadoPedido.PAGO_PENDIENTE;
        }
        
        pedidoRepository.actualizarEstadoPedido(pago.getPedidoId(), estadoPedido);
        
        logger.info("Webhook procesado: pago={}, estado={}, pedido={}", 
            pago.getId(), estadoPago, pago.getPedidoId());
    }
    
    /**
     * Endpoint de consulta de estado de un pago (para polling del frontend).
     */
    public EstadoPagoResponse consultarEstado(Long pagoId) {
        Pago pago = pagoRepository.obtenerPorId(pagoId)
            .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
        
        return EstadoPagoResponse.builder()
            .pagoId(pago.getId())
            .pedidoId(pago.getPedidoId())
            .estado(pago.getEstado())
            .webhookStatus(pago.getWebhookStatus())
            .fechaConfirmacion(pago.getFechaConfirmacion())
            .mensaje(pago.getMensajeError() != null ? pago.getMensajeError() : 
                    pago.getEstado() == EstadoPago.PENDIENTE ? "En verificación..." : "Completado")
            .build();
    }
    
    // Métodos auxiliares
    
    private String mapearAWompiType(MetodoPago metodo) {
        return switch (metodo) {
            case PSE -> "BANK_TRANSFER";
            case TARJETA_CREDITO -> "CARD";
            case TARJETA_DEBITO -> "CARD";
            case NEQUI -> "NEQUI";
            default -> throw new ValidationException("Método de pago no soportado: " + metodo);
        };
    }
    
    private EstadoPago mapearEstadoWompi(String statusWompi) {
        return switch (statusWompi.toUpperCase()) {
            case "APPROVED" -> EstadoPago.APROBADO;
            case "DECLINED" -> EstadoPago.RECHAZADO;
            case "PENDING" -> EstadoPago.PENDIENTE;
            case "VOIDED" -> EstadoPago.CANCELADO;
            default -> EstadoPago.PENDIENTE;
        };
    }
    
    private WompiWebhookPayload parsePayload(String json) throws WompiException {
        try {
            return new ObjectMapper().readValue(json, WompiWebhookPayload.class);
        } catch (Exception e) {
            throw new WompiException("Error al parsear webhook JSON", e);
        }
    }
}
```

#### Paso 4.4: Crear PagoController

**Archivo:** `src/main/java/com/web/prime_drip_club/controllers/PagoController.java` (NUEVO)

```java
@RestController
@RequestMapping("/v1/pagos")
@RequiredArgsConstructor
public class PagoController {
    
    private final PagoService pagoService;
    private final PagoRepository pagoRepository;
    
    /**
     * Inicia un pago para un pedido.
     * Requiere JWT del usuario logueado.
     * Retorna datos de continuación hacia Wompi.
     */
    @PostMapping("/iniciar")
    public ResponseEntity<Response<IniciarPagoResponse>> iniciarPago(
            @Valid @RequestBody IniciarPagoRequest request,
            @AuthenticationPrincipal UserDetailsImpl user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Response.<IniciarPagoResponse>builder()
                    .responseCode(401)
                    .success(false)
                    .message("Se requiere autenticación")
                    .build()
            );
        }
        
        try {
            IniciarPagoResponse response = pagoService.iniciarPago(request, user.getUsuario().getId());
            
            return ResponseEntity.ok(
                Response.<IniciarPagoResponse>builder()
                    .responseCode(200)
                    .success(true)
                    .data(response)
                    .message("Pago iniciado exitosamente")
                    .build()
            );
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(
                Response.<IniciarPagoResponse>builder()
                    .responseCode(400)
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                Response.<IniciarPagoResponse>builder()
                    .responseCode(500)
                    .success(false)
                    .message("Error al iniciar pago: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Webhook endpoint: recibe eventos de Wompi.
     * Acceso: público (validado por firma).
     */
    @PostMapping("/webhook/wompi")
    public ResponseEntity<Void> webhookWompi(
            @RequestBody String payloadJson,
            @RequestHeader(value = "X-Wompi-Signature", required = false) String signature) {
        
        try {
            pagoService.procesarWebhook(payloadJson, signature);
            return ResponseEntity.ok().build();
        } catch (WompiException e) {
            // Log pero responde 200 para que Wompi no reintente
            logger.error("Error al procesar webhook Wompi: {}", e.getMessage());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error inesperado en webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Consulta estado de un pago (polling).
     * Requiere JWT del usuario
     */
    @GetMapping("/estado")
    public ResponseEntity<Response<EstadoPagoResponse>> consultarEstado(
            @RequestParam Long pagoId,
            @AuthenticationPrincipal UserDetailsImpl user) {
        
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                Response.<EstadoPagoResponse>builder()
                    .responseCode(401)
                    .success(false)
                    .message("Se requiere autenticación")
                    .build()
            );
        }
        
        try {
            EstadoPagoResponse response = pagoService.consultarEstado(pagoId);
            
            // Validar que el pago pertenece a un pedido del usuario
            // (consultarEstado debe incluir esta validación)
            
            return ResponseEntity.ok(
                Response.<EstadoPagoResponse>builder()
                    .responseCode(200)
                    .success(true)
                    .data(response)
                    .build()
            );
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Response.<EstadoPagoResponse>builder()
                    .responseCode(404)
                    .success(false)
                    .message(e.getMessage())
                    .build()
            );
        }
    }
}
```

---

### Fase 5: Validaciones y Endurecimiento

**Objetivo:** Agregar reglas de negocio y seguridad para MVP.

**Checklist:**
- ✅ No permitir checkout de carrito vacío.
- ✅ Validación de stock disponible en momento del checkout.
- ✅ No permitir iniciar pago si pedido no está en `PAGO_PENDIENTE`.
- ✅ Validación de firma en webhooks de Wompi.
- ✅ Idempotencia en procesamiento de webhooks (no duplicar confirmaciones).
- ✅ Coherencia de monto: validar que monto en transacción Wompi = total del pedido.
- ✅ Restricción de reintentos: usar `idempotency_key` para evitar duplicados.
- ✅ Manejo de timeout: si webhook no llega en X minutos, cambiar estado a expirado.

---

### Fase 6: Pruebas

#### Pruebas Unitarias

- **PedidoService:**
  - Carrito vacío → ValidationException
  - Stock insuficiente → ValidationException
  - Checkout exitoso → respuesta con pedidoId y totales correctos
  - Rollback en caso de error → carrito no se limpia

- **PagoService:**
  - Webhook aprobado → estado pago = APROBADO, estado pedido = PAGADO
  - Webhook rechazado → estado pago = RECHAZADO, estado pedido = PAGO_PENDIENTE (reintentar)
  - Webhook duplicado → sin cambios (idempotencia)
  - Firma inválida → excepción

#### Pruebas de Integración

1. **Flujo anónimo completo:**
   - Crear carrito → agregar items → checkout → iniciar pago → webhook aprobado → consultar estado final

2. **Flujo logueado completo:**
   - Login → crear carrito (vinculado a usuario) → agregar items → checkout → iniciar pago → webhook → estado final

3. **Manejo de errores:**
   - Stock insuficiente al checkout
   - Pedido ya pagado al intentar otro pago
   - Firma de webhook inválida
   - Timeout de transacción

#### Pruebas Manuales

- Con sandbox de Wompi validar flujos de PSE, tarjeta y Nequi.
- Validar que email de confirmación se genera post-webhook.
- Validar consulta de estado desde frontend (polling).

---

## Dictamen de Archivos

### Nuevos (crear)

```
src/main/java/com/web/prime_drip_club/
├── controllers/
│   └── PedidoController.java            ← Checkout y consulta
│   └── PagoController.java              ← Iniciación y webhook
├── service/
│   └── PedidoService.java               ← Lógica transaccional checkout
│   └── PagoService.java                 ← Lógica de pagos Wompi
├── repository/
│   ├── PedidoRepository.java            ← Interfaz
│   ├── PagoRepository.java              ← Interfaz
│   └── impl/
│       ├── PedidoRepositoryImpl.java     ← JDBC
│       └── PagoRepositoryImpl.java       ← JDBC
├── dto/
│   ├── pedido/
│   │   ├── CheckoutRequest.java
│   │   ├── CheckoutResponse.java
│   │   ├── DireccionCheckoutRequest.java
│   ├── pago/
│   │   ├── IniciarPagoRequest.java
│   │   ├── IniciarPagoResponse.java
│   │   ├── EstadoPagoResponse.java
├── integration/
│   └── wompi/
│       ├── WompiClient.java             ← Cliente HTTP
│       ├── WompiConfig.java             ← Configuración
│       ├── WompiException.java          ← Excepciones custom
│       ├── request/
│       │   ├── WompiCreateTransactionRequest.java
│       │   └── ...
│       └── response/
│           ├── WompiCreateTransactionResponse.java
│           ├── WompiWebhookPayload.java
│           └── ...

src/main/resources/
└── db/migration/
    └── V16__alter_pago_add_wompi_tracking_fields.sql
```

### Modificar

- [src/main/java/com/web/prime_drip_club/config/security/WebSecurityConfig.java](src/main/java/com/web/prime_drip_club/config/security/WebSecurityConfig.java) → Agregar rutas públicas
- [src/main/resources/application.properties](src/main/resources/application.properties) → Agregar config Wompi

---

## Verificación Final

### Checklist Previo a MVP

- [ ] Migraciones `V16` ejecutadas sin errores en BD local
- [ ] Modelos `Pago`, `Pedido` compilados correctamente
- [ ] `PedidoService.crearPedidoDesdeCarrito()` transaccional
- [ ] `PagoService.procesarWebhook()` idempotente
- [ ] Endpoints `/v1/pedidos/checkout`, `/v1/pagos/iniciar`, `/v1/pagos/webhook/wompi` expuestos y públicos
- [ ] JWT validados en controladores (guestToken no necesario, flujo registrados)
- [ ] Prueba de integración anónimo complete (carrito → pedido → pago → webhook)
- [ ] Prueba de integración logueado complete
- [ ] Validación de stock y carrito vacío en checkout
- [ ] Firma de webhook de Wompi validada
- [ ] Logs auditables sin exponer secretos Wompi

### Comandos de Verificación

```bash
# Compilar y ejecutar migraciones locales
./mvnw clean compile flyway:migrate

# Ejecutar pruebas unitarias
./mvnw test -Dtest=PedidoServiceTest,PagoServiceTest

# Ejecutar pruebas de integración
./mvnw test -Dtest=*IntegrationTest

# Iniciar aplicación
./mvnw spring-boot:run
```

---

## Próximos Pasos Post-MVP

1. **Antifraude:** Integrar detección de anomalías (montos altos, velocidad de compra).
2. **Email/SMS:** Notificaciones de confirmación de pedido y pago.
3. **Backoffice:** Dashboard de pagos, reports, manual approval de transacciones dudosas.
4. **Reembolsos:** Lógica de creación de crédito/nota de débito.
5. **Multimoneda:** Soportar USD, EUR (solo si expanden mercado).
6. **Logística:** Integración con proveedores de envío para etiquetas y tracking.

---

**Versión:** 1.0
**Fecha:** 23 de marzo de 2026
**Estado:** Listo para implementación Fase 1
