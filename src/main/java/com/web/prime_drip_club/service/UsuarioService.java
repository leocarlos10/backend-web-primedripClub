package com.web.prime_drip_club.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.prime_drip_club.config.security.UserDetailsImpl;
import com.web.prime_drip_club.config.security.jwt.JwtUtils;
import com.web.prime_drip_club.dto.Usuario.LoginRequest;
import com.web.prime_drip_club.dto.Usuario.LoginResponse;
import com.web.prime_drip_club.dto.Usuario.RegisterRequest;
import com.web.prime_drip_club.dto.Usuario.RegisterResponse;
import com.web.prime_drip_club.exception.ValidationException;
import com.web.prime_drip_club.models.Usuario;
import com.web.prime_drip_club.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if(usuarioRepository.existsByEmail(request.getEmail())){
           throw new ValidationException("El email ya esta registrado");
        }

        Usuario nuevoUsuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .password(passwordEncoder.encode(request.getPassword())) 
                .activo(true)
                .build();
        
        Long usuarioId = usuarioRepository.save(nuevoUsuario);
        // 1L rol de usuario normal
        usuarioRepository.updateRol(usuarioId, 1L);

        return RegisterResponse.builder()
                .nombre(nuevoUsuario.getNombre())
                .email(nuevoUsuario.getEmail())
                .message("Usuario registrado exitosamente.")
                .build();
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtils.getToken(userDetails);

        /*
         * getAuthorities devuelve una lista de GrantedAuthority
         * [SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN")],
         * .stream convierte la coleccion en un flujo para procesarla
         * .map(auth -> auth.getAuthority()) transforma cada GrantedAuthority en su representación String
         * .toList() recoge los resultados en una lista de String
         */
        return LoginResponse.builder()
                .id(userDetails.getUsuario().getId())
                .nombre(userDetails.getUsuario().getNombre())
                .email(userDetails.getUsuario().getEmail())
                .roles(userDetails.getAuthorities().stream()
                        .map(auth -> auth.getAuthority())
                        .toList())
                .token(token)
                .tokenType("Bearer")
                .build();
    }


    
}
