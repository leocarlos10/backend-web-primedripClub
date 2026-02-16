package com.web.prime_drip_club.dto.pedido;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PedidoRequest {
    @NotBlank(message = "El email de contacto es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String emailContacto;

    @NotBlank(message = "El nombre de contacto es obligatorio")
    private String nombreContacto;

    @Size(max = 20)
    private String telefono;

    @NotNull(message = "La dirección de envío es obligatoria")
    private Long direccionId;

    private String notas;

    @NotNull(message = "Los items del pedido son obligatorios")
    private List<DetallePedidoRequest> items;
}
