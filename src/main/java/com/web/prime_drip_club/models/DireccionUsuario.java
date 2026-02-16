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
public class DireccionUsuario {
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
