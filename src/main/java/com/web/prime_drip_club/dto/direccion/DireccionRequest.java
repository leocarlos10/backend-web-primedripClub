package com.web.prime_drip_club.dto.direccion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DireccionRequest {
    @NotBlank(message = "El alias es obligatorio")
    @Size(max = 50, message = "El alias no puede superar 50 caracteres")
    private String alias;

    @NotBlank(message = "La dirección completa es obligatoria")
    private String direccionCompleta;

    @NotBlank(message = "La ciudad es obligatoria")
    @Size(max = 100)
    private String ciudad;

    @NotBlank(message = "El departamento es obligatorio")
    @Size(max = 100)
    private String departamento;

    @Size(max = 10)
    private String codigoPostal;

    @Size(max = 20)
    private String telefonoContacto;

    private Boolean esPrincipal;
}
