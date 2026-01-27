package com.web.prime_drip_club.dto.Usuario;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String nombre;
    private String email;
    private List<String> roles;
    private String token;
    private String tokenType;
    
}
