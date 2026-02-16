package com.web.prime_drip_club.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.prime_drip_club.dto.carrito.ActualizarCantidadRequest;
import com.web.prime_drip_club.dto.carrito.DetalleCarritoRequest;
import com.web.prime_drip_club.dto.carrito.DetalleCarritoResponse;
import com.web.prime_drip_club.dto.carrito.EliminarDetalleCarritoRequest;
import com.web.prime_drip_club.repository.DetalleCarritoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DetalleCarritoService {

    private final DetalleCarritoRepository detalleCarritoRepository;


    @Transactional
    public DetalleCarritoResponse guardarDetalleCarrito(DetalleCarritoRequest request) {

        Long detalleCarritoId = detalleCarritoRepository.guardarDetalleCarrito(request);
        return DetalleCarritoResponse.builder()
                .id(detalleCarritoId)
                .carritoId(request.getCarritoId())
                .productoId(request.getProductoId())
                .cantidad(request.getCantidad())
                .build();
    }


    @Transactional
    public Boolean actualizarCantidadDetalleCarrito(ActualizarCantidadRequest request) {
        return detalleCarritoRepository.actualizarCantidadDetalleCarrito(request);
    }

    @Transactional
    public Boolean eliminarDetalleCarrito(EliminarDetalleCarritoRequest request) {
        return detalleCarritoRepository.eliminarDetalleCarrito(request);
    }
    
}
