package com.web.prime_drip_club.repository;

import com.web.prime_drip_club.models.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoRepository {
    List<Producto> findAll();

    List<Producto> findByActivo(Boolean activo);

    List<Producto> findByCategoriaId(Long categoriaId);

    Optional<Producto> findById(Long id);

    Long save(Producto producto);

    Boolean update(Producto producto);

    Boolean delete(Long id);
}
