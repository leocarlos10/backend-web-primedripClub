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
    private Long id;
    private Long usuarioId;
    private Long direccionId;
    private String direccionEnvioSnapshot;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private EstadoPedido estado;
    private String notas;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
