package com.web.prime_drip_club.repository.impl;

import java.util.List;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.web.prime_drip_club.dto.direccion.DireccionRequest;
import com.web.prime_drip_club.dto.direccion.DireccionResponse;
import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.repository.DireccionRespository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class DireccionRespositoryImpl implements DireccionRespository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<DireccionResponse> direccionRowMapper = (rs, rowNum) -> DireccionResponse.builder()
            .id(rs.getLong("id"))
            .usuarioId(rs.getLong("usuario_id"))
            .alias(rs.getString("alias"))
            .direccionCompleta(rs.getString("direccion_completa"))
            .ciudad(rs.getString("ciudad"))
            .departamento(rs.getString("departamento"))
            .codigoPostal(rs.getString("codigo_postal"))
            .telefonoContacto(rs.getString("telefono_contacto"))
            .esPrincipal(rs.getBoolean("es_principal"))
            .activa(rs.getBoolean("activa"))
            .fechaCreacion(
                    rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toLocalDateTime()
                            : null)
            .fechaActualizacion(rs.getTimestamp("fecha_actualizacion") != null
                    ? rs.getTimestamp("fecha_actualizacion").toLocalDateTime()
                    : null)
            .build();

    @Override
    public Boolean crearDireccion(DireccionRequest request) {
        String sql = "INSERT INTO direccion_usuario (usuario_id, alias, direccion_completa, ciudad, departamento, codigo_postal, telefono_contacto, es_principal, fecha_creacion, fecha_actualizacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";
        try {
            int row = jdbcTemplate.update(sql,
                    request.getUsuarioId(),
                    request.getAlias(),
                    request.getDireccionCompleta(),
                    request.getCiudad(),
                    request.getDepartamento(),
                    request.getCodigoPostal(),
                    request.getTelefonoContacto(),
                    request.getEsPrincipal());
            return row > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DatabaseException("error al crear la direccion");
        }
    }

    @Override
    public List<DireccionResponse> listarDirecciones(Long usuarioId) {
        String sql = "SELECT * FROM direccion_usuario WHERE usuario_id = ? AND activa = TRUE";
        try {
            return jdbcTemplate.query(sql, direccionRowMapper, usuarioId);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DatabaseException("error al listar las direcciones");
        }
    }

    @Override
    public DireccionResponse obtenerDireccion(Long id) {
        String sql = "SELECT * FROM direccion_usuario WHERE id = ? AND activa = TRUE";
        try {
            return jdbcTemplate.queryForObject(sql, direccionRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DatabaseException("error al obtener la direccion");
        }
    }

    @Override
    public Boolean actualizarDireccion(Long id, DireccionRequest request) {
        String sql = "UPDATE direccion_usuario SET alias = ?, direccion_completa = ?, ciudad = ?, departamento = ?, codigo_postal = ?, telefono_contacto = ?, es_principal = ?, fecha_actualizacion = NOW() WHERE id = ? AND activa = TRUE";
        try {
            int row = jdbcTemplate.update(sql,
                    request.getAlias(),
                    request.getDireccionCompleta(),
                    request.getCiudad(),
                    request.getDepartamento(),
                    request.getCodigoPostal(),
                    request.getTelefonoContacto(),
                    request.getEsPrincipal(),
                    id);
            return row > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DatabaseException("error al actualizar la direccion");
        }
    }

    @Override
    public Boolean eliminarDireccion(Long id) {
        // Soft delete: marcamos como inactiva en lugar de eliminar físicamente
        String sql = "UPDATE direccion_usuario SET activa = FALSE, fecha_actualizacion = NOW() WHERE id = ? AND activa = TRUE";
        try {
            int row = jdbcTemplate.update(sql, id);
            return row > 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new DatabaseException("error al eliminar la direccion");
        }
    }
}
