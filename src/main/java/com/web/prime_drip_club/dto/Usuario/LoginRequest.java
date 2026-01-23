package com.web.prime_drip_club.dto.Usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank
    @Size(max = 100)
    @Email(message = "El email debe tener un formato válido")
    String email;
    @NotBlank
    @Size(min = 6, max = 100)
    String password;
    
}
