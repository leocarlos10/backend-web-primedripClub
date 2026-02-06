package com.web.prime_drip_club.controllers;

import com.web.prime_drip_club.dto.categoria.CategoriaRequest;
import com.web.prime_drip_club.dto.categoria.CategoriaResponse;
import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Obtener todas las categorías
     * Endpoint público - Cualquier usuario puede ver las categorías
     */
    @GetMapping
    public ResponseEntity<Response<List<CategoriaResponse>>> obtenerTodas() {
        List<CategoriaResponse> categorias = categoriaService.obtenerTodas();
        Response<List<CategoriaResponse>> response = Response.<List<CategoriaResponse>>builder()
                .responseCode(200)
                .success(true)
                .message("Categorías obtenidas exitosamente")
                .data(categorias)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Obtener una categoría por ID
     * Endpoint público - Cualquier usuario puede ver una categoría específica
     */
    @GetMapping("/{id}")
    public ResponseEntity<Response<CategoriaResponse>> obtenerPorId(@PathVariable Long id) {
        CategoriaResponse categoria = categoriaService.obtenerPorId(id);
        Response<CategoriaResponse> response = Response.<CategoriaResponse>builder()
                .responseCode(200)
                .success(true)
                .message("Categoría obtenida exitosamente")
                .data(categoria)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Crear una nueva categoría
     * Solo administradores pueden crear categorías
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<CategoriaResponse>> crear(@Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse categoria = categoriaService.crear(request);
        Response<CategoriaResponse> response = Response.<CategoriaResponse>builder()
                .responseCode(201)
                .success(true)
                .message("Categoría creada exitosamente")
                .data(categoria)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Actualizar una categoría existente
     * Solo administradores pueden actualizar categorías
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<CategoriaResponse>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequest request) {
        CategoriaResponse categoria = categoriaService.actualizar(id, request);
        Response<CategoriaResponse> response = Response.<CategoriaResponse>builder()
                .responseCode(200)
                .success(true)
                .message("Categoría actualizada exitosamente")
                .data(categoria)
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Eliminar una categoría
     * Solo administradores pueden eliminar categorías
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response<Void>> eliminar(@PathVariable Long id) {
        categoriaService.eliminar(id);
        Response<Void> response = Response.<Void>builder()
                .responseCode(200)
                .success(true)
                .message("Categoría eliminada exitosamente")
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
