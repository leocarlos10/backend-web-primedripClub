package com.web.prime_drip_club.dto.pedido;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

import com.web.prime_drip_club.models.EstadoPedido;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoRequest {

    @NotNull(message = "El Id del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El total del pedido es obligatorio")
    private BigDecimal total;

    @NotNull(message = "El subtotal del pedido es obligatorio")
    private BigDecimal subtotal;

    private BigDecimal costoEnvio;
    @NotNull(message = "El estado del pedido es obligatorio")
    private EstadoPedido estado;

    @NotNull(message = "La dirección de envío es obligatoria")
    private Long direccionId;

    private String notas;

    @NotNull(message = "Los items del pedido son obligatorios")
    private List<DetallePedidoRequest> items;
}
