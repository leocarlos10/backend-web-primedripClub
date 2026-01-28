package com.web.prime_drip_club.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.web.prime_drip_club.dto.Usuario.LoginRequest;
import com.web.prime_drip_club.dto.Usuario.LoginResponse;
import com.web.prime_drip_club.dto.Usuario.RegisterRequest;
import com.web.prime_drip_club.dto.Usuario.RegisterResponse;
import com.web.prime_drip_club.dto.common.Response;
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

    /**
     * Endpoint para autenticar un usuario en el sistema.
     * 
     * <p>
     * Este endpoint valida las credenciales del usuario (correo y contraseña) y
     * retorna un token JWT junto con la información del usuario autenticado.
     * </p>
     * 
     * @param request Objeto LoginRequest que contiene las credenciales del usuario
     * @return ResponseEntity con el siguiente formato:
     * 
     * <pre>
     * {
     *   "responseCode": 200,
     *   "success": true,
     *   "message": "Login exitoso",
     *   "data": {
     *      "nombre": "Juan Pérez",
     *      "email": "juan.perez@example.com",
     *      "roles": ["ROLE_USER"]
     *      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *       "tokenType" : "Bearer
     *     }
     *   }
     * }
     * </pre>
     * 
     * @throws ValidationException si las credenciales son inválidas
     */
    @PostMapping("/login")
    public ResponseEntity<Response<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse responseService = usuarioService.login(request);
        Response<LoginResponse> response = Response.<LoginResponse>builder()
                .responseCode(200)
                .success(true)
                .data(responseService)
                .message("Login exitoso")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para registrar un nuevo usuario en el sistema.
     * 
     * <p>
     * Este endpoint crea una nueva cuenta de usuario con la información
     * proporcionada,
     * asigna roles predeterminados y retorna la información del usuario registrado
     * junto
     * con un token JWT para acceso inmediato.
     * </p>
     * 
     * @param request Objeto RegisterRequest que contiene los datos del nuevo
     *                usuario
     * @return ResponseEntity con el siguiente formato:
     * 
     * <pre>
     * {
     *   "responseCode": 200,
     *   "success": true,
     *   "message": "Registro exitoso",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "usuario": {
     *       "id": 2,
     *       "nombre": "María González",
     *       "email": "maria.gonzalez@example.com",
     *       "telefono": "+34612345678",
     *       "roles": ["ROLE_USER"]
     *     }
     *   }
     * }
     * </pre>
     * 
     * @throws ValidationException si los datos proporcionados son inválidos
     * @throws DatabaseException   si el email ya está registrado en el sistema
     */
    @PostMapping("/register")
    public ResponseEntity<Response<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse responseService = usuarioService.register(request);
        Response<RegisterResponse> response = Response.<RegisterResponse>builder()
                .responseCode(200)
                .success(true)
                .data(responseService)
                .message("Registro exitoso")
                .build();
        return ResponseEntity.ok(response);
    }

}
