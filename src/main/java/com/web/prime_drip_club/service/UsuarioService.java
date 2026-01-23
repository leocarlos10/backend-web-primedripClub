package com.web.prime_drip_club.service;

import org.springframework.stereotype.Service;

import com.web.prime_drip_club.dto.Usuario.RegisterRequest;
import com.web.prime_drip_club.dto.Usuario.RegisterResponse;
import com.web.prime_drip_club.models.Usuario;
import com.web.prime_drip_club.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public RegisterResponse register(RegisterRequest request) {

        if(usuarioRepository.existsByEmail(request.getEmail())){
            return RegisterResponse.builder()
                    .email(request.getEmail())
                    .message("El email ya está registrado.")
                    .build();
        }

        // harcodear la contraseña por ahora va asi
        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .password(request.getPassword()) // Aquí deberías hashear la contraseña
                .activo(true)
                .build();
        
        Long usuarioId = usuarioRepository.save(nuevoUsuario);
        // 1L rol de usuario normal
        usuarioRepository.updateRol(usuarioId, 1L);

        return RegisterResponse.builder()
                .name(nuevoUsuario.getNombre())
                .email(nuevoUsuario.getEmail())
                .message("Usuario registrado exitosamente.")
                .build();
    }
    
}
