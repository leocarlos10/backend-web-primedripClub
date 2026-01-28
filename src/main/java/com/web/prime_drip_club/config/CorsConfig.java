package com.web.prime_drip_club.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de CORS (Cross-Origin Resource Sharing) para la aplicación.
 * 
 * <p>
 * Esta configuración permite que el frontend (React) pueda comunicarse con el
 * backend (Spring Boot) desde un origen diferente (diferente dominio, puerto o
 * protocolo).
 * </p>
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos - añade aquí las URLs de tu frontend
        // En desarrollo: http://localhost:3000, http://localhost:5173 (Vite), etc.
        // En producción: tu dominio real
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000", // React por defecto
                "http://localhost:5173" // Vite
        ));
        // "https://tu-dominio.com" // Añadir tu dominio de producción

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"));

        // Headers permitidos - permite todos los headers comunes
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"));

        // Headers expuestos - permite que el frontend acceda a estos headers
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type"));

        // Permite el envío de cookies y credenciales
        configuration.setAllowCredentials(true);

        // Tiempo de cache de la configuración CORS (en segundos)
        configuration.setMaxAge(3600L);

        // Aplica esta configuración a todos los endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
