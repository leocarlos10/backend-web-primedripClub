package com.web.prime_drip_club.controllers;

import com.web.prime_drip_club.dto.producto.ProductoRequest;
import com.web.prime_drip_club.dto.producto.ProductoResponse;
import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    public ResponseEntity<Response<ProductoResponse>> obtenerPorId(@PathVariable Long id) {
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
            @PathVariable Long id,
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Boolean>> eliminar(@PathVariable Long id) {
        Boolean estado = productoService.eliminar(id);
        Response<Boolean> response = Response.<Boolean>builder()
                .responseCode(200)
                .success(true)
                .message("Producto eliminado exitosamente")
                .data(estado)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
