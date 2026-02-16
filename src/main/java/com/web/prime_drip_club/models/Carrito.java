package com.web.prime_drip_club.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrito {
    private Long id;
    private Long usuarioId;
    private String sessionId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
