package com.web.prime_drip_club.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetallePedido {
    private Integer id;
    private Integer pedidoId;
    private Integer productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
}
