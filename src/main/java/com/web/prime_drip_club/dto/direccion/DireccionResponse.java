package com.web.prime_drip_club.dto.direccion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DireccionResponse {
    private Long id;
    private Long usuarioId;
    private String alias;
    private String direccionCompleta;
    private String ciudad;
    private String departamento;
    private String codigoPostal;
    private String telefonoContacto;
    private Boolean esPrincipal;
    private Boolean activa;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
