package com.web.prime_drip_club.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    /**
     * Guarda una imagen en el servidor y retorna la URL relativa
     */
    public String saveImage(MultipartFile file) throws IOException {
        // 1. Validar que sea una imagen
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen (JPG, PNG, GIF, WEBP)");
        }

        // 2. Validar tamaño máximo (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("La imagen no puede superar 5MB");
        }

        // 3. Crear directorio si no existe
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 4. Generar nombre único para evitar colisiones
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName != null ? originalFileName.substring(originalFileName.lastIndexOf("."))
                : ".jpg";
        String fileName = System.currentTimeMillis() + "-" + UUID.randomUUID() + fileExtension;

        // 5. Guardar archivo en el sistema
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 6. Retornar URL relativa que se guardará en la BD
        return "/uploads/images/" + fileName;
    }

    /**
     * Elimina una imagen del servidor
     * 
     * @return true si la imagen fue eliminada, false si no existía o no se pudo
     *         eliminar
     */
    public boolean deleteImage(String imageUrl) throws IOException {
        if (imageUrl != null && imageUrl.startsWith("/uploads/images/")) {
            String fileName = imageUrl.substring("/uploads/images/".length());
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            if (!filePath.startsWith(Paths.get(uploadDir).toAbsolutePath())) {
                throw new SecurityException("Intento de acceso fuera del directorio permitido");
            }
            return Files.deleteIfExists(filePath);
        }
        return false;
    }
}
