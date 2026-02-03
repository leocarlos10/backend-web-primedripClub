package com.web.prime_drip_club.dto.categoria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
}
