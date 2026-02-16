package com.web.prime_drip_club.repository;

import java.util.Optional;

import com.web.prime_drip_club.dto.carrito.CarritoRequest;
import com.web.prime_drip_club.dto.carrito.CarritoResponse;

public interface CarritoRespository {
    Long guardarCarrito(CarritoRequest carrito);
    Optional<CarritoResponse> obtenerCarrito(Long carritoId, Long usuarioId, String sessionId);

}
