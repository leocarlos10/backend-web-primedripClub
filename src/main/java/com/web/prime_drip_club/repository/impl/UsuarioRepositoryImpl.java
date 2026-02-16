package com.web.prime_drip_club.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import java.sql.PreparedStatement;
import java.sql.Statement;
import org.springframework.stereotype.Repository;
import com.web.prime_drip_club.models.Usuario;
import com.web.prime_drip_club.repository.UsuarioRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.web.prime_drip_club.exception.DatabaseException;

@Repository
@Transactional
@RequiredArgsConstructor
public class UsuarioRepositoryImpl implements UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public Usuario mapRowUsuario(ResultSet rs) throws SQLException {
        return Usuario.builder()
                .id(rs.getLong("id"))
                .nombre(rs.getString("nombre"))
                .email(rs.getString("email"))
                .telefono(rs.getString("telefono"))
                .password(rs.getString("password"))
                .activo(rs.getBoolean("activo"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime())
                .build();
    }

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try {
            Usuario user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowUsuario(rs), email);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new DatabaseException("Error al buscar el usuario por email: " + e.getMessage(), e);
        }
    }

    public Boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM usuario WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public Long save(Usuario usuario) {
        String sql = "INSERT INTO usuario (nombre, email, telefono, password, activo, fecha_creacion) VALUES (?, ?, ?, ?, ?, NOW())";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, usuario.getNombre());
                ps.setString(2, usuario.getEmail());
                ps.setString(3, usuario.getTelefono());
                ps.setString(4, usuario.getPassword());
                ps.setBoolean(5, usuario.getActivo());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseException("Error al guardar el usuario: No se generó un ID");
            }
            return key.longValue();
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar el usuario: " + e.getMessage(), e);
        }
    }

    public Boolean updateRol(Long usuarioId, Long rolId) {
        String sql = "INSERT INTO usuario_rol (usuario_id, rol_id) VALUES (?, ?)";
        try {
            int rows = jdbcTemplate.update(sql, usuarioId, rolId);
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al asignar el rol al usuario: " + e.getMessage(), e);
        }
    }

    public Optional<Usuario> findById(Long id) {
        String sql = "SELECT * FROM usuario WHERE id = ?";
        try {
            Usuario user = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowUsuario(rs), id);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            throw new DatabaseException("Error al buscar el usuario por ID: " + e.getMessage(), e);
        }
    }

}
