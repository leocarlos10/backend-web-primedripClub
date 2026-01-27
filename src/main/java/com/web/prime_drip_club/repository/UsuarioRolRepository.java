package com.web.prime_drip_club.repository;

import java.util.List;

public interface UsuarioRolRepository {
    boolean saveUsuarioRol(Long usuarioId, Integer rolId);
    List<String> getRolesByUsuarioId(Long usuarioId);
    
}
