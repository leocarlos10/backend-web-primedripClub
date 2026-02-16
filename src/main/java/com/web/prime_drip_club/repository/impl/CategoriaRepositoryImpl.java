package com.web.prime_drip_club.repository.impl;

import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.models.Categoria;
import com.web.prime_drip_club.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CategoriaRepositoryImpl implements CategoriaRepository {

    private final JdbcTemplate jdbcTemplate;

    private Categoria mapRowToCategoria(ResultSet rs) throws SQLException {
        return Categoria.builder()
                .id(rs.getLong("id"))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .build();
    }

    @Override
    public List<Categoria> findAll() {
        String sql = "SELECT * FROM categoria ORDER BY nombre ASC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToCategoria(rs));
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener categorías: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Categoria> findById(Long id) {
        String sql = "SELECT * FROM categoria WHERE id = ?";
        try {
            Categoria categoria = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToCategoria(rs), id);
            return Optional.ofNullable(categoria);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Categoria> findByNombre(String nombre) {
        String sql = "SELECT * FROM categoria WHERE nombre = ?";
        try {
            Categoria categoria = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToCategoria(rs), nombre);
            return Optional.ofNullable(categoria);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Long save(Categoria categoria) {
        String sql = "INSERT INTO categoria (nombre, descripcion) VALUES (?, ?)";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, categoria.getNombre());
                ps.setString(2, categoria.getDescripcion());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseException("Error al guardar categoría: No se generó un ID");
            }
            return key.longValue();
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean update(Categoria categoria) {
        String sql = "UPDATE categoria SET nombre = ?, descripcion = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql,
                    categoria.getNombre(),
                    categoria.getDescripcion(),
                    categoria.getId());
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al actualizar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean delete(Long id) {
        String sql = "DELETE FROM categoria WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al eliminar categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean existsByNombre(String nombre) {
        String sql = "SELECT COUNT(*) FROM categoria WHERE nombre = ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nombre);
            return count != null && count > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al verificar existencia de categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean existsByNombreAndNotId(String nombre, Long id) {
        String sql = "SELECT COUNT(*) FROM categoria WHERE nombre = ? AND id != ?";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, nombre, id);
            return count != null && count > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al verificar existencia de categoría: " + e.getMessage(), e);
        }
    }
}
