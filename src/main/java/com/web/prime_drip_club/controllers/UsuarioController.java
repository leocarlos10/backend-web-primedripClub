package com.web.prime_drip_club.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.prime_drip_club.dto.Usuario.LoginRequest;
import com.web.prime_drip_club.dto.Usuario.RegisterRequest;
import com.web.prime_drip_club.dto.Usuario.RegisterResponse;
import com.web.prime_drip_club.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(usuarioService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = usuarioService.register(request);
        return ResponseEntity.ok(response);
    }
    
    
    
}
