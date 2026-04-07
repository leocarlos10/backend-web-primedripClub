package com.web.prime_drip_club.repository.impl;

import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.web.prime_drip_club.dto.pedido.PedidoRequest;
import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.repository.PedidoRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PedidoRepositoryImpl implements PedidoRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Long crearPedido(PedidoRequest pedido) {
        
        String sql = "INSERT INTO pedido (usuario_id, total, subtotal, costo_envio, estado, fecha_creacion, fecha_actualizacion, direccion_id) VALUES (?, ?, ?, ?, ?, NOW(), NOW(), ?)";

        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setLong(1, pedido.getUsuarioId());
                ps.setBigDecimal(2, pedido.getTotal());
                ps.setBigDecimal(3, pedido.getSubtotal());
                ps.setBigDecimal(4, pedido.getCostoEnvio());
                ps.setString(5, pedido.getEstado().toString());
                ps.setLong(6, pedido.getDireccionId());
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseException("Error al guardar el pedido: No se generó un ID");
            }

            return key.longValue();
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar el pedido: " + e.getMessage(), e);
        }
    }
}
