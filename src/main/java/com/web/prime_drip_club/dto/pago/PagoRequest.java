package com.web.prime_drip_club.dto.pago;

import com.web.prime_drip_club.models.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PagoRequest {
    @NotNull(message = "El pedido es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodo;

    private String referencia;

    private String metadata;
}
