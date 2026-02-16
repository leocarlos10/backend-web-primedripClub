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
public class DetalleCarrito {
    private Long id;
    private Long carritoId;
    private Long productoId;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private LocalDateTime fechaAgregado;
}
