package com.web.prime_drip_club.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.web.prime_drip_club.config.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

        private final JwtAuthenticationFilter jwtAuthenticationFilter;
        private final UserDetailsService userDetailsService;
        private final CorsConfigurationSource corsConfigurationSource;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                return http
                                .csrf(AbstractHttpConfigurer::disable)
                                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                                .authorizeHttpRequests(req -> req
                                                .requestMatchers("/v1/auth/**").permitAll()
                                                .requestMatchers("/uploads/**").permitAll() // Permitir acceso público a imágenes
                                                .requestMatchers("/v1/categorias").permitAll() // Listar categorías
                                                .requestMatchers("/v1/categorias/*").permitAll() // Ver categoría por ID
                                                .requestMatchers("/v1/productos/activos").permitAll() // Listar productos activos
                                                .requestMatchers("/v1/productos/*").permitAll() // Ver producto por ID
                                                .requestMatchers("/v1/carrito/**").permitAll() // Permitir acceso público a carrito
                                                .requestMatchers("/v1/detalle-carrito/**").permitAll() // Permitir acceso público a detalle carrito
                                                .anyRequest().authenticated())
                                .sessionManagement(sessionManager -> sessionManager
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .userDetailsService(userDetailsService)
                                .build();

        }

}
