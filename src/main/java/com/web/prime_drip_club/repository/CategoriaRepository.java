package com.web.prime_drip_club.repository;

import com.web.prime_drip_club.models.Categoria;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
    List<Categoria> findAll();

    Optional<Categoria> findById(Long id);

    Optional<Categoria> findByNombre(String nombre);

    Long save(Categoria categoria);

    Boolean update(Categoria categoria);

    Boolean delete(Long id);

    Boolean existsByNombre(String nombre);

    Boolean existsByNombreAndNotId(String nombre, Long id);
}
