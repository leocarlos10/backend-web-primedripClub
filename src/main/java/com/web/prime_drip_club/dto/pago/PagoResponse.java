package com.web.prime_drip_club.dto.pago;

import com.web.prime_drip_club.models.EstadoPago;
import com.web.prime_drip_club.models.MetodoPago;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagoResponse {
    private Long id;
    private Long pedidoId;
    private MetodoPago metodo;
    private EstadoPago estado;
    private String referencia;
    private String mensajeError;
    private String metadata;
    private LocalDateTime fechaPago;
}
