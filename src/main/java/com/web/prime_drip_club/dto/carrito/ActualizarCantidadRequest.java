package com.web.prime_drip_club.dto.carrito;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ActualizarCantidadRequest {
    @NotNull
    private Long carritoId;
    
    @NotNull
    private Long productoId;
    
    @NotNull
    @Positive
    private Integer cantidad;
    @Nullable
    private Long usuarioId;
    @Nullable
    private String sessionId;
} 
