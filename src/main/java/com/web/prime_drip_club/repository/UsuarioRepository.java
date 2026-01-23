package com.web.prime_drip_club.repository;

import com.web.prime_drip_club.models.Usuario;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> findByEmail(String email);
    Boolean existsByEmail(String email);
    Long save(Usuario usuario);
    Boolean updateRol(Long id , Long rol);
    Optional<Usuario> findById(Long id);
}
