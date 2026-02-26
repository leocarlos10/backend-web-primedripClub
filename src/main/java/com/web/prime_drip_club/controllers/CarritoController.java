package com.web.prime_drip_club.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web.prime_drip_club.dto.carrito.CarritoRequest;
import com.web.prime_drip_club.dto.carrito.CarritoResponse;
import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.service.CarritoService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/v1/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;

    @GetMapping
    public ResponseEntity<Response<CarritoResponse>> obtenerCarrito(
            @RequestParam("carritoId") Long carritoId,
            @RequestParam(value = "usuarioId", required = false) Long usuarioId,
            @RequestParam(value = "sessionId", required = false) String sessionId) {

        if (usuarioId == null && (sessionId == null || sessionId.isBlank())) {
            Response<CarritoResponse> error = Response.<CarritoResponse>builder()
                    .responseCode(400)
                    .success(false)
                    .data(null)
                    .message("Se requiere usuarioId o sessionId")
                    .build();
            return ResponseEntity.badRequest().body(error);
        }

        CarritoResponse carrito = carritoService.obtenerCarrito(carritoId, usuarioId, sessionId);
        Response<CarritoResponse> response = Response.<CarritoResponse>builder()
                .responseCode(200)
                .success(true)
                .data(carrito)
                .message("Carrito obtenido exitosamente")
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<Boolean>> actualizarCarrito(@RequestParam Long carritoId, @RequestParam Long usuarioId) {
        return carritoService.actualizarCarrito(carritoId, usuarioId);
    }

    @PostMapping
    public ResponseEntity<Response<CarritoResponse>> guardarCarrito(@RequestBody CarritoRequest request) {

        CarritoResponse carritoResponse = carritoService.guardarCarrito(request);

        Response<CarritoResponse> response = Response.<CarritoResponse>builder()
                .responseCode(200)
                .success(true)
                .data(carritoResponse)
                .message("Carrito creado exitosamente")
                .build();

        return ResponseEntity.ok(response);
    }
}
