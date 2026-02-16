package com.web.prime_drip_club.dto.carrito;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EliminarDetalleCarritoRequest {
    @NotNull(message = "El id del carrito es obligatorio")
    private Long carritoId;
    @NotNull(message = "El id del producto es obligatorio")
    private Long productoId;
    @Nullable
    private Long usuarioId;
    @Nullable
    private String sessionId;
    
}
