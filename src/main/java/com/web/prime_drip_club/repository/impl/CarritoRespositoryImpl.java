package com.web.prime_drip_club.repository.impl;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import com.web.prime_drip_club.dto.carrito.CarritoRequest;
import com.web.prime_drip_club.dto.carrito.CarritoResponse;
import com.web.prime_drip_club.dto.carrito.DetalleCarritoResponse;
import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.repository.CarritoRespository;

@Repository
public class CarritoRespositoryImpl implements CarritoRespository {

    private final JdbcTemplate jdbcTemplate;

    public CarritoRespositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Long guardarCarrito(CarritoRequest carrito) {
        // SQL: Inserta usuario_id o session_id (uno de los dos debe ser NOT NULL según
        // constraint)
        String sql = "INSERT INTO carrito (usuario_id, session_id, fecha_creacion, fecha_actualizacion) VALUES (?, ?, NOW(), NOW())";

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

                // Si usuarioId no es null, inserta el usuario_id, si no deja NULL
                if (carrito.getUsuarioId() != null) {
                    ps.setLong(1, carrito.getUsuarioId());
                } else {
                    ps.setNull(1, java.sql.Types.BIGINT);
                }

                // Si sessionId no es null, inserta el session_id, si no deja NULL
                if (carrito.getSessionId() != null) {
                    ps.setString(2, carrito.getSessionId());
                } else {
                    ps.setNull(2, java.sql.Types.VARCHAR);
                }

                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseException("Error al guardar el carrito: No se generó un ID");
            }
            return key.longValue();
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar el carrito: " + e.getMessage(), e);
        }
    }

    /**
     * Actuliza un carrito existente asignandole un usuarioId
     * @param carritoId El ID del carrito a actualizar
     * @param usuarioId El ID del usuario a asignar al carrito
     * @return true si se actualizó correctamente, false si no se encontró el carrito o no se pudo actualizar
     */
    @Override
    public Boolean actualizarCarrito(Long carritoId, Long usuarioId) {
        
        String sql = "UPDATE carrito SET usuario_id = ? , session_id = NULL, fecha_actualizacion = NOW() WHERE id = ? ";
        try {
            int rows = jdbcTemplate.update(sql, usuarioId, carritoId);
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al actualizar el carrito: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Long> obtenerCarritoId(Long usuarioId) {
        String sql = """
                    SELECT
                        c.id
                    FROM carrito c
                    WHERE c.usuario_id = ?
                """;
        try {
            Long carritoId = jdbcTemplate.queryForObject(sql, Long.class, usuarioId);
            return Optional.of(carritoId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<CarritoResponse> obtenerCarrito(Long carritoId, Long usuarioId, String sessionId) {

        String sql = """
                    SELECT
                        c.id,
                        c.usuario_id,
                        c.session_id,
                        c.fecha_creacion,
                        c.fecha_actualizacion,
                        dc.id AS detalle_id,
                        dc.carrito_id,
                        dc.producto_id,
                        p.nombre,
                        p.imagen_url,
                        p.marca,
                        p.stock,
                        p.categoria_id,
                        dc.cantidad,
                        dc.precio_unitario,
                        dc.fecha_agregado
                    FROM carrito c
                    LEFT JOIN detalle_carrito dc ON c.id = dc.carrito_id
                    LEFT JOIN producto p ON p.id = dc.producto_id
                    WHERE c.id = ?
                      AND (c.usuario_id = ? OR c.session_id = ?)
                """;
        try {
            CarritoResponse carrito = jdbcTemplate.query(sql, rs -> {
                CarritoResponse response = null;
                /* recorremos cada fila dela respuesta del left join */
                while (rs.next()) {
                    if (response == null) {
                        response = new CarritoResponse();
                        response.setId(rs.getLong("id"));
                        response.setCarritoId(rs.getLong("carrito_id"));
                        response.setUsuarioId(rs.getLong("usuario_id"));
                        response.setSessionId(rs.getString("session_id"));
                        response.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                        response.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
                        response.setItems(new ArrayList<>());
                    }

                    // Si hay detalle (LEFT JOIN puede traer nulls)
                    Long detalleId = rs.getLong("detalle_id");
                    if (!rs.wasNull()) {
                        DetalleCarritoResponse detalle = new DetalleCarritoResponse();
                        detalle.setId(detalleId);
                        detalle.setCarritoId(rs.getLong("carrito_id"));
                        detalle.setProductoId(rs.getLong("producto_id"));
                        detalle.setProductoNombre(rs.getString("nombre"));
                        detalle.setProductoImagenUrl(rs.getString("imagen_url"));
                        detalle.setCantidad(rs.getInt("cantidad"));
                        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                        detalle.setMarca(rs.getString("marca"));
                        detalle.setStock(rs.getInt("stock"));
                        detalle.setCategoriaId(rs.getLong("categoria_id"));
                        detalle.setFechaAgregado(rs.getTimestamp("fecha_agregado").toLocalDateTime());
                        response.getItems().add(detalle);
                    }
                }
                return response;
            }, carritoId, usuarioId, sessionId);

            return Optional.ofNullable(carrito);
        } catch (Exception e) {
            throw new DatabaseException("Error al obtener el carrito: " + e.getMessage(), e);
        }
    }
}
