package com.web.prime_drip_club.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarritoResponse {
    private Long id;
    private Long usuarioId;
    private String sessionId;
    private List<DetalleCarritoResponse> items;
    private Long carritoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
