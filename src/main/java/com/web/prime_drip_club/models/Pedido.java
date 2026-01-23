package com.web.prime_drip_club.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {
    private Integer id;
    private Integer usuarioId;
    private String emailContacto;
    private String nombreContacto;
    private String telefono;
    private String direccionEnvio;
    private BigDecimal total;
    private String estado;
    private LocalDateTime fechaCreacion;
}
