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
public class Producto {
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String marca;
    private String imagenUrl;
    private Boolean activo;
    private Long categoriaId;
    private EtiquetaProducto etiqueta;
    private SexoProducto sexo;
    private Boolean isFeatured;
    private LocalDateTime fechaCreacion;
}
