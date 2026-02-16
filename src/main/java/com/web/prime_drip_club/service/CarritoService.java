package com.web.prime_drip_club.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.web.prime_drip_club.dto.carrito.CarritoRequest;
import com.web.prime_drip_club.dto.carrito.CarritoResponse;
import com.web.prime_drip_club.exception.ResourceNotFoundException;
import com.web.prime_drip_club.repository.CarritoRespository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRespository carritoRespository;
    
    @Transactional
    public CarritoResponse guardarCarrito(CarritoRequest request) {

        if(request.getUsuarioId() == null && request.getSessionId() == null){
            request.setSessionId(UUID.randomUUID().toString());
        } else if(request.getUsuarioId() != null && request.getSessionId() != null){
            throw new IllegalArgumentException("No se puede proporcionar ambos usuarioId y sessionId");
        }
        
        Long carritoId = carritoRespository.guardarCarrito(request);

        return CarritoResponse.builder()
                .carritoId(carritoId)
                .usuarioId(request.getUsuarioId())
                .sessionId(request.getSessionId())
                .build();
    }

    @Transactional(readOnly = true)
    public CarritoResponse obtenerCarrito(Long carritoId, Long usuarioId, String sessionId) {
        return  carritoRespository.obtenerCarrito(carritoId, usuarioId, sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("carrito no encontrado"));
    }
    
}
