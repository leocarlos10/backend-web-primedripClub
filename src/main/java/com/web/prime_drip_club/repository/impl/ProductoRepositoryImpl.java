package com.web.prime_drip_club.repository.impl;

import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.models.Producto;
import com.web.prime_drip_club.repository.ProductoRepository;
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
public class ProductoRepositoryImpl implements ProductoRepository {

    private final JdbcTemplate jdbcTemplate;

    private Producto mapRowToProducto(ResultSet rs) throws SQLException {
        String etiquetaValor = rs.getString("etiqueta");
        return Producto.builder()
                .id(rs.getLong("id"))
                .nombre(rs.getString("nombre"))
                .descripcion(rs.getString("descripcion"))
                .precio(rs.getBigDecimal("precio"))
                .stock(rs.getInt("stock"))
                .marca(rs.getString("marca"))
                .imagenUrl(rs.getString("imagen_url"))
                .activo(rs.getBoolean("activo"))
                .categoriaId(rs.getLong("categoria_id"))
                .etiqueta(
                        etiquetaValor != null ? com.web.prime_drip_club.models.EtiquetaProducto.fromValor(etiquetaValor)
                                : null)
                .isFeatured(rs.getBoolean("is_featured"))
                .fechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime())
                .build();
    }

    @Override
    public List<Producto> findAll() {
        String sql = "SELECT * FROM producto ORDER BY fecha_creacion DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProducto(rs));
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener productos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> findByActivo(Boolean activo) {
        String sql = "SELECT * FROM producto WHERE activo = ? ORDER BY fecha_creacion DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProducto(rs), activo);
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener productos activos: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Producto> findByCategoriaId(Long categoriaId) {
        String sql = "SELECT * FROM producto WHERE categoria_id = ? ORDER BY fecha_creacion DESC";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToProducto(rs), categoriaId);
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener productos por categoría: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Producto> findById(Long id) {
        String sql = "SELECT * FROM producto WHERE id = ?";
        try {
            Producto producto = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToProducto(rs), id);
            return Optional.ofNullable(producto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Long save(Producto producto) {
        String sql = "INSERT INTO producto (nombre, descripcion, precio, stock, marca, " +
                "imagen_url, activo, categoria_id, etiqueta, is_featured, fecha_creacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, producto.getNombre());
                ps.setString(2, producto.getDescripcion());
                ps.setBigDecimal(3, producto.getPrecio());
                ps.setInt(4, producto.getStock());
                ps.setString(5, producto.getMarca());
                ps.setString(6, producto.getImagenUrl());
                ps.setBoolean(7, producto.getActivo());
                ps.setLong(8, producto.getCategoriaId());
                ps.setString(9, producto.getEtiqueta() != null ? producto.getEtiqueta().getValor() : null);
                ps.setBoolean(10, producto.getIsFeatured() != null ? producto.getIsFeatured() : false);
                return ps;
            }, keyHolder);

            return keyHolder.getKey().longValue();
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean update(Producto producto) {
        String sql = "UPDATE producto SET nombre = ?, descripcion = ?, precio = ?, " +
                "stock = ?, marca = ?, imagen_url = ?, activo = ?, categoria_id = ?, " +
                "etiqueta = ?, is_featured = ? WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql,
                    producto.getNombre(),
                    producto.getDescripcion(),
                    producto.getPrecio(),
                    producto.getStock(),
                    producto.getMarca(),
                    producto.getImagenUrl(),
                    producto.getActivo(),
                    producto.getCategoriaId(),
                    producto.getEtiqueta() != null ? producto.getEtiqueta().getValor() : null,
                    producto.getIsFeatured() != null ? producto.getIsFeatured() : false,
                    producto.getId());
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al actualizar producto: " + e.getMessage(), e);
        }
    }

    @Override
    public Boolean delete(Long id) {
        String sql = "DELETE FROM producto WHERE id = ?";
        try {
            int rows = jdbcTemplate.update(sql, id);
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al eliminar producto: " + e.getMessage(), e);
        }
    }
}