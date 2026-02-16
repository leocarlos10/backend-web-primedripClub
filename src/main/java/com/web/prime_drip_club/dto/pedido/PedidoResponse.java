package com.web.prime_drip_club.dto.pedido;

import com.web.prime_drip_club.models.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoResponse {
    private Long id;
    private Long usuarioId;
    private String emailContacto;
    private String nombreContacto;
    private String telefono;
    private Long direccionId;
    private String direccionEnvioSnapshot;
    private BigDecimal total;
    private BigDecimal subtotal;
    private BigDecimal costoEnvio;
    private EstadoPedido estado;
    private String notas;
    private List<DetallePedidoResponse> items;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
