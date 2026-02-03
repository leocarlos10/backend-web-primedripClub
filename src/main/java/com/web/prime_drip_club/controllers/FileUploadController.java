package com.web.prime_drip_club.controllers;
import com.web.prime_drip_club.dto.common.Response;
import com.web.prime_drip_club.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;



@RestController
@RequestMapping("/v1/upload")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // solo admins pueden acceder a estos endpoinds
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Endpoint para subir imágenes de productos
     * 
     * @param file Archivo de imagen (JPG, PNG, GIF, WEBP)
     * @return Response con la URL de la imagen guardada
     * 
     * Ejemplo de respuesta exitosa:
     * {
     *   "responseCode": 200,
     *   "success": true,
     *   "message": "Imagen subida exitosamente",
     *   "data": {
     *     "imageUrl": "/uploads/images/1234567890-abc.jpg"
     *   }
     * }
     */
    @PostMapping("/product-image")
    public ResponseEntity<Response<String>> uploadProductImage(
            @RequestParam("image") MultipartFile file) {
        try {
            // Guardar imagen y obtener URL
            String imageUrl = fileStorageService.saveImage(file);
            
            
            Response<String> response = Response.<String>builder()
                    .responseCode(200)
                    .success(true)
                    .message("Imagen subida exitosamente")
                    .data(imageUrl)
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Error de validación (tamaño o tipo de archivo)
            Response<String> response = Response.<String>builder()
                    .responseCode(400)
                    .success(false)
                    .message(e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            // Error inesperado
            Response<String> response = Response.<String>builder()
                    .responseCode(500)
                    .success(false)
                    .message("Error al subir la imagen: " + e.getMessage())
                    .data(null)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @DeleteMapping("/delete-image")
    public ResponseEntity<Response<Boolean>> deleteImage(@RequestParam("imageUrl") String imageUrl) {
        
        try {
            boolean eliminada = fileStorageService.deleteImage(imageUrl);

            if(eliminada){
                Response<Boolean> response = Response.<Boolean>builder()
                        .responseCode(200)
                        .success(true)
                        .message("Imagen eliminada exitosamente")
                        .data(true)
                        .build();
                return ResponseEntity.ok(response);

            } else {
                Response<Boolean> response = Response.<Boolean>builder()
                        .responseCode(404)
                        .success(false)
                        .message("La imagen no existe o no se pudo eliminar")
                        .data(false)
                        .build();
                return ResponseEntity.status(404).body(response);
            }
            
        } catch (Exception e) {
            Response<Boolean> response = Response.<Boolean>builder()
                    .responseCode(500)
                    .success(false)
                    .message("Error al eliminar la imagen: " + e.getMessage())
                    .data(false)
                    .build();
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
}
