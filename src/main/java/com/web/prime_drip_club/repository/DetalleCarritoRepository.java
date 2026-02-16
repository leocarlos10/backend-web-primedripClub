package com.web.prime_drip_club.repository;

import com.web.prime_drip_club.dto.carrito.ActualizarCantidadRequest;
import com.web.prime_drip_club.dto.carrito.DetalleCarritoRequest;
import com.web.prime_drip_club.dto.carrito.EliminarDetalleCarritoRequest;

public interface DetalleCarritoRepository {
    Long guardarDetalleCarrito(DetalleCarritoRequest detalleCarrito);
    Boolean actualizarCantidadDetalleCarrito(ActualizarCantidadRequest request);
    Boolean eliminarDetalleCarrito(EliminarDetalleCarritoRequest request);
}
