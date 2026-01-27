package com.web.prime_drip_club.repository.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.web.prime_drip_club.exception.DatabaseException;
import com.web.prime_drip_club.models.Rol;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RolRepositoryImpl {

    private final JdbcTemplate jdbcTemplate;

    public Rol mapRowRol(ResultSet rs ) throws SQLException {
        return Rol.builder()
                .id(rs.getInt("id"))
                .nombre(rs.getString("nombre"))
                .build();
    }

    public Boolean saveRol(String nombre){
        String sql = "INSERT INTO rol (nombre) VALUES (?)";
        try {
            int rows = jdbcTemplate.update(sql, nombre);
            return rows > 0;
        } catch (Exception e) {
            throw new DatabaseException("Error al guardar el rol");
        }
    }
}
