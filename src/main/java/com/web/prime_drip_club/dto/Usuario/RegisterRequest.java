package com.web.prime_drip_club.dto.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank
    String nombre;
    @NotBlank
    @Email(message = "El email debe tener un formato válido")
    String email;
    @NotBlank
    String password;
    @NotNull
    Boolean activo;
}
