package com.web.prime_drip_club.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.dto.direccion.DireccionRequest;
import com.web.prime_drip_club.dto.direccion.DireccionResponse;
import com.web.prime_drip_club.service.DireccionService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/v1/direcciones")
@RequiredArgsConstructor
public class DireccionesController {

    private final DireccionService direccionService;

    @PostMapping
    public ResponseEntity<Response<Boolean>> crearDireccion(@RequestBody DireccionRequest request) {
        return direccionService.crearDireccion(request);
    }

    @GetMapping
    public ResponseEntity<Response<List<DireccionResponse>>> listarDirecciones(@RequestParam Long usuarioId) {
        return direccionService.listarDirecciones(usuarioId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<DireccionResponse>> obtenerDireccion(@PathVariable Long id) {
        return direccionService.obtenerDireccion(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<Boolean>> actualizarDireccion(@PathVariable Long id,
            @RequestBody DireccionRequest request) {
        return direccionService.actualizarDireccion(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Boolean>> eliminarDireccion(@PathVariable Long id) {
        return direccionService.eliminarDireccion(id);
    }
}
