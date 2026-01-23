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
public class Pago {
    private Integer id;
    private Integer pedidoId;
    private String metodo;
    private String estado;
    private String referencia;
    private LocalDateTime fechaPago;
}
