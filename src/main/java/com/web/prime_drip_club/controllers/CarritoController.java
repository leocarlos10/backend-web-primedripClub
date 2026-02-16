package com.web.prime_drip_club.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
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



@RestController
@RequestMapping("/v1/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carritoService;
    

    @GetMapping
    public ResponseEntity<Response<CarritoResponse>> obtenerCarrito( @RequestBody CarritoRequest request) {
        CarritoResponse carrito = carritoService.obtenerCarrito(request.getCarritoId(), request.getUsuarioId(), request.getSessionId());
        Response<CarritoResponse> response = Response.<CarritoResponse>builder()
                .responseCode(200)
                .success(true)
                .data(carrito)
                .message("Carrito obtenido exitosamente")
                .build();
        return ResponseEntity.ok(response);
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
