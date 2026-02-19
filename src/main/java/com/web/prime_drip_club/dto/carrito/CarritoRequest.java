package com.web.prime_drip_club.dto.carrito;

import jakarta.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarritoRequest {
    
    @Nullable
    private Long usuarioId;
    @Nullable
    private String sessionId;

}
