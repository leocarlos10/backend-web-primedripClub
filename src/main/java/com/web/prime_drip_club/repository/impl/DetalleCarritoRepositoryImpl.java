package com.web.prime_drip_club.repository.impl;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.web.prime_drip_club.dto.carrito.ActualizarCantidadRequest;
import com.web.prime_drip_club.dto.carrito.DetalleCarritoRequest;
import com.web.prime_drip_club.dto.carrito.EliminarDetalleCarritoRequest;
import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.repository.DetalleCarritoRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DetalleCarritoRepositoryImpl implements DetalleCarritoRepository {

    private final JdbcTemplate jdbcTemplate;

    /**
     * Guarda un detalle de carrito en la base de datos.
     * <p>
     * Si el producto ya existe en el carrito (mismo carrito_id y producto_id),
     * automáticamente suma la cantidad nueva a la existente y actualiza el precio
     * unitario.
     * Esto se logra mediante la cláusula ON DUPLICATE KEY UPDATE de MySQL.
     * </p>
     * <p>
     * <b>Comportamiento:</b>
     * <ul>
     * <li>Inserción nueva: Si el producto no existe en el carrito, lo inserta con
     * la cantidad especificada</li>
     * <li>Actualización: Si el producto ya existe, suma las cantidades
     * (cantidad_existente + cantidad_nueva)</li>
     * <li>Actualiza el precio_unitario con el valor más reciente</li>
     * </ul>
     * </p>
     * <p>
     * <b>Ejemplo:</b>
     * 
     * <pre>
     * Carrito tiene: Producto A con cantidad 2
     * Se agrega: Producto A con cantidad 3
     * Resultado: Producto A con cantidad 5
     * </pre>
     * </p>
     *
     * @param detalleCarrito objeto DetalleCarritoRequest que contiene:
     *                       carritoId, productoId, cantidad y precioUnitario
     * @return Long el ID del detalle del carrito (nuevo o actualizado)
     * @throws DatabaseException si ocurre un error al guardar en la base de datos
     * @see DetalleCarritoRequest
     */
    @Override
    public Long guardarDetalleCarrito(DetalleCarritoRequest detalleCarrito) {
        String sql = """
                INSERT INTO detalle_carrito
                (carrito_id, producto_id, cantidad, precio_unitario, fecha_agregado)
                VALUES (?, ?, ?, ?, NOW())
                ON DUPLICATE KEY UPDATE
                    cantidad = cantidad + VALUES(cantidad),
                    precio_unitario = VALUES(precio_unitario)
                """;

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, detalleCarrito.getCarritoId());
                ps.setLong(2, detalleCarrito.getProductoId());
                ps.setInt(3, detalleCarrito.getCantidad());
                ps.setBigDecimal(4, detalleCarrito.getPrecioUnitario());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseException("Error al guardar el detalle del carrito: No se generó un ID");
            }
            
            return key.longValue();
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar el detalle del carrito: " + e.getMessage(), e);
        }
    }
    

    @Override
    public Boolean actualizarCantidadDetalleCarrito(ActualizarCantidadRequest request) {
        String sql = """
                UPDATE detalle_carrito dc
                INNER JOIN carrito c ON dc.carrito_id = c.id
                SET dc.cantidad = ?
                WHERE dc.carrito_id = ?
                AND dc.producto_id = ?
                AND (c.usuario_id = ? OR c.session_id = ?)
                """;
        int rows = jdbcTemplate.update(sql, request.getCantidad(), request.getCarritoId(), request.getProductoId(), request.getUsuarioId(), request.getSessionId());
        return (rows > 0);
    }

    @Override
    public Boolean eliminarDetalleCarrito(EliminarDetalleCarritoRequest request) {
        String sql = """
                DELETE dc FROM detalle_carrito dc
                INNER JOIN carrito c ON dc.carrito_id = c.id
                WHERE dc.carrito_id = ?
                AND dc.producto_id = ?
                AND (c.usuario_id = ? OR c.session_id = ?)
                """;
        int rows = jdbcTemplate.update(sql, request.getCarritoId(), request.getProductoId(), request.getUsuarioId(), request.getSessionId());
        return (rows > 0);
    }
}
