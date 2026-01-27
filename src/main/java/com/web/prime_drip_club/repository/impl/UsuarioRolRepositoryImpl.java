package com.web.prime_drip_club.repository.impl;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.repository.UsuarioRolRepository;

import lombok.RequiredArgsConstructor;
@Repository
@RequiredArgsConstructor
public class UsuarioRolRepositoryImpl implements UsuarioRolRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean saveUsuarioRol(Long usuarioId, Integer rolId) {
        try {
            String sql = "INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)";
            int rows = jdbcTemplate.update(sql, usuarioId, rolId);
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar el usuario_rol: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> getRolesByUsuarioId(Long usuarioId) {
        String sql = "SELECT r.nombre FROM rol r " +
                     "JOIN usuario_rol ur ON r.id = ur.rol_id " +
                     "WHERE ur.usuario_id = ?";
         try {
             return jdbcTemplate.queryForList(sql, String.class, usuarioId);
         } catch (Exception e) {
             throw new DatabaseException("Error al obtener los roles del usuario: " + e.getMessage(), e);
         }
    }
    
}
