package com.web.prime_drip_club.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.prime_drip_club.dto.carrito.ActualizarCantidadRequest;
import com.web.prime_drip_club.dto.carrito.DetalleCarritoRequest;
import com.web.prime_drip_club.dto.carrito.DetalleCarritoResponse;
import com.web.prime_drip_club.dto.carrito.EliminarDetalleCarritoRequest;
import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.service.DetalleCarritoService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/v1/detalle-carrito")
@RequiredArgsConstructor
public class DetalleCarritoController {

    private final DetalleCarritoService detalleCarritoService;

    @PostMapping
    public ResponseEntity<Response<DetalleCarritoResponse>> guardarDetalleCarrito(
            @RequestBody DetalleCarritoRequest request) {
        DetalleCarritoResponse detalleCarritoResponse = detalleCarritoService.guardarDetalleCarrito(request);

        Response<DetalleCarritoResponse> response = Response.<DetalleCarritoResponse>builder()
                .responseCode(200)
                .success(true)
                .data(detalleCarritoResponse)
                .message("Detalle carrito creado exitosamente")
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<Response<Boolean>> actualizarCantidad(@RequestBody ActualizarCantidadRequest request) {
        Boolean actualizado = detalleCarritoService.actualizarCantidadDetalleCarrito(request);
        Response<Boolean> response;
        if (actualizado) {
            response = Response.<Boolean>builder()
                    .responseCode(200)
                    .success(actualizado)
                    .data(actualizado)
                    .message("Cantidad del detalle del carrito actualizada exitosamente")
                    .build();
            return ResponseEntity.ok(response);
        } else {
            response = Response.<Boolean>builder()
                    .responseCode(400)
                    .success(actualizado)
                    .data(actualizado)
                    .message("la cantidad del detalle no se pudoo actualizar")
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping
    public ResponseEntity<Response<Boolean>> eliminarDetalleCarrito(
            @RequestBody EliminarDetalleCarritoRequest request) {
        Boolean eliminado = detalleCarritoService.eliminarDetalleCarrito(request);
        Response<Boolean> response;
        if(eliminado){
             response = Response.<Boolean>builder()
                    .responseCode(200)
                    .success(eliminado)
                    .data(eliminado)
                    .message(eliminado ? "Detalle del carrito eliminado exitosamente"
                            : "No se pudo eliminar el detalle del carrito")
                    .build();

            return ResponseEntity.ok(response);
        }else {
                response = Response.<Boolean>builder()
                    .responseCode(400)
                    .success(eliminado)
                    .data(eliminado)
                    .message( "No se pudo eliminar el detalle del carrito")
                    .build();

            return ResponseEntity.ok(response);
        }
    }

}
