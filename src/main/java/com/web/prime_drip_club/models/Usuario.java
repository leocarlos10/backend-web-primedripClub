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
public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String password;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

}
