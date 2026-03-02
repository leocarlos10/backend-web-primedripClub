package com.web.prime_drip_club.repository;

import java.util.List;

import com.web.prime_drip_club.dto.direccion.DireccionRequest;
import com.web.prime_drip_club.dto.direccion.DireccionResponse;

public interface DireccionRespository {

    Boolean crearDireccion(DireccionRequest request);

    List<DireccionResponse> listarDirecciones(Long usuarioId);

    DireccionResponse obtenerDireccion(Long id);

    Boolean actualizarDireccion(Long id, DireccionRequest request);

    Boolean eliminarDireccion(Long id);
}