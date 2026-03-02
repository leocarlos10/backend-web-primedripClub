package com.web.prime_drip_club.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.dto.direccion.DireccionRequest;
import com.web.prime_drip_club.dto.direccion.DireccionResponse;
import com.web.prime_drip_club.repository.DireccionRespository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DireccionService {

    private final DireccionRespository direccionRespository;

    @Transactional
    public ResponseEntity<Response<Boolean>> crearDireccion(DireccionRequest request) {
        Boolean isCreated = direccionRespository.crearDireccion(request);
        if (!isCreated) {
            return ResponseEntity.badRequest().body(Response.<Boolean>builder()
                    .success(false)
                    .message("No se pudo crear la dirección")
                    .data(false)
                    .build());
        }
        return ResponseEntity.ok(Response.<Boolean>builder()
                .success(true)
                .message("Dirección creada exitosamente")
                .data(true)
                .build());
    }

    public ResponseEntity<Response<List<DireccionResponse>>> listarDirecciones(Long usuarioId) {
        List<DireccionResponse> direcciones = direccionRespository.listarDirecciones(usuarioId);
        return ResponseEntity.ok(Response.<List<DireccionResponse>>builder()
                .success(true)
                .message("Direcciones obtenidas exitosamente")
                .data(direcciones)
                .build());
    }

    public ResponseEntity<Response<DireccionResponse>> obtenerDireccion(Long id) {
        DireccionResponse direccion = direccionRespository.obtenerDireccion(id);
        if (direccion == null) {
            return ResponseEntity.status(404).body(Response.<DireccionResponse>builder()
                    .success(false)
                    .message("Dirección no encontrada")
                    .data(null)
                    .build());
        }
        return ResponseEntity.ok(Response.<DireccionResponse>builder()
                .success(true)
                .message("Dirección obtenida exitosamente")
                .data(direccion)
                .build());
    }

    @Transactional
    public ResponseEntity<Response<Boolean>> actualizarDireccion(Long id, DireccionRequest request) {
        Boolean isUpdated = direccionRespository.actualizarDireccion(id, request);
        if (!isUpdated) {
            return ResponseEntity.status(404).body(Response.<Boolean>builder()
                    .success(false)
                    .message("Dirección no encontrada o no se pudo actualizar")
                    .data(false)
                    .build());
        }
        return ResponseEntity.ok(Response.<Boolean>builder()
                .success(true)
                .message("Dirección actualizada exitosamente")
                .data(true)
                .build());
    }

    @Transactional
    public ResponseEntity<Response<Boolean>> eliminarDireccion(Long id) {
        Boolean isDeleted = direccionRespository.eliminarDireccion(id);
        if (!isDeleted) {
            return ResponseEntity.status(404).body(Response.<Boolean>builder()
                    .success(false)
                    .message("Dirección no encontrada o ya fue eliminada")
                    .data(false)
                    .build());
        }
        return ResponseEntity.ok(Response.<Boolean>builder()
                .success(true)
                .message("Dirección eliminada exitosamente")
                .data(true)
                .build());
    }
}
