package com.web.prime_drip_club.service;

import com.web.prime_drip_club.dto.categoria.CategoriaRequest;
import com.web.prime_drip_club.dto.categoria.CategoriaResponse;
import com.web.prime_drip_club.exception.ValidationException;
import com.web.prime_drip_club.models.Categoria;
import com.web.prime_drip_club.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    /**
     * Obtener todas las categorías
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::convertirAResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una categoría por ID
     */
    @Transactional(readOnly = true)
    public CategoriaResponse obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Categoría no encontrada con ID: " + id));
        return convertirAResponse(categoria);
    }

    /**
     * Crear una nueva categoría
     */
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        // Validar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new ValidationException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();

        Long id = categoriaRepository.save(categoria);
        categoria.setId(id);

        return convertirAResponse(categoria);
    }

    /**
     * Actualizar una categoría existente
     */
    @Transactional
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        // Verificar que la categoría existe
             categoriaRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Categoría no encontrada con ID: " + id));

        // Validar que no exista otra categoría con el mismo nombre
        if (categoriaRepository.existsByNombreAndNotId(request.getNombre(), id)) {
            throw new ValidationException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        Categoria categoriaActualizada = Categoria.builder()
                .id(id)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();

        categoriaRepository.update(categoriaActualizada);
        return convertirAResponse(categoriaActualizada);
    }

    /**
     * Eliminar una categoría por ID
     */
    @Transactional
    public void eliminar(Long id) {
        // Verificar que la categoría existe
         categoriaRepository.findById(id)
                .orElseThrow(() -> new ValidationException("Categoría no encontrada con ID: " + id));

        boolean eliminada = categoriaRepository.delete(id);

        if (!eliminada) {
            throw new ValidationException("No se pudo eliminar la categoría con ID: " + id);
        }
    }

    /**
     * Convertir entidad Categoria a CategoriaResponse
     */
    private CategoriaResponse convertirAResponse(Categoria categoria) {
        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }
}
