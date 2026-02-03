package com.web.prime_drip_club.service;

import com.web.prime_drip_club.dto.producto.ProductoRequest;
import com.web.prime_drip_club.dto.producto.ProductoResponse;
import com.web.prime_drip_club.exception.ResourceNotFoundException;
import com.web.prime_drip_club.models.Producto;
import com.web.prime_drip_club.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final FileStorageService fileStorageService;

    public List<ProductoResponse> obtenerTodos() {
        return productoRepository.findAll().stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public List<ProductoResponse> obtenerActivos() {
        return productoRepository.findByActivo(true).stream()
                .map(this::convertirAResponse)
                .toList();
    }

    public ProductoResponse obtenerPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return convertirAResponse(producto);
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .marca(request.getMarca())
                .imagenUrl(request.getImagenUrl())
                .activo(request.getActivo())
                .categoriaId(request.getCategoriaId())
                .build();

        Long id = productoRepository.save(producto);
        producto.setId(id);

        return convertirAResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Si cambió la imagen, eliminar la anterior
        if (!productoExistente.getImagenUrl().equals(request.getImagenUrl())) {
            try {
                fileStorageService.deleteImage(productoExistente.getImagenUrl());
            } catch (Exception e) {
                // Log pero continuar
                System.err.println("Error al eliminar imagen anterior: " + e.getMessage());
            }
        }

        Producto productoActualizado = Producto.builder()
                .id(id)
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .stock(request.getStock())
                .marca(request.getMarca())
                .imagenUrl(request.getImagenUrl())
                .activo(request.getActivo())
                .categoriaId(request.getCategoriaId())
                .fechaCreacion(productoExistente.getFechaCreacion())
                .build();

        productoRepository.update(productoActualizado);
        return convertirAResponse(productoActualizado);
    }

    @Transactional
    public Boolean eliminar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        // Eliminar imagen
        try {
            fileStorageService.deleteImage(producto.getImagenUrl());
        } catch (Exception e) {
            System.err.println("Error al eliminar imagen: " + e.getMessage());
        }

        /* devuelve true si se elimino false si no se elimino */
        return productoRepository.delete(id);
    }

    private ProductoResponse convertirAResponse(Producto producto) {
        return ProductoResponse.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .descripcion(producto.getDescripcion())
                .precio(producto.getPrecio())
                .stock(producto.getStock())
                .marca(producto.getMarca())
                .imagenUrl(producto.getImagenUrl())
                .activo(producto.getActivo())
                .categoriaId(producto.getCategoriaId())
                .fechaCreacion(producto.getFechaCreacion())
                .build();
    }
}
