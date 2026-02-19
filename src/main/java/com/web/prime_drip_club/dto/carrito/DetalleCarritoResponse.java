package com.web.prime_drip_club.dto.carrito;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetalleCarritoResponse {
    private Long id;
    private Long carritoId;
    private Long productoId;
    private String productoNombre;
    private String productoImagenUrl;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private String marca;
    private Integer stock;
    private Long categoriaId;
    private LocalDateTime fechaAgregado;
}
