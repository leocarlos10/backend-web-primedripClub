package com.web.prime_drip_club.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoPedido {
    PENDIENTE("Pendiente"),
    PAGO_PENDIENTE("Pago pendiente"),
    PAGADO("Pagado"),
    PROCESANDO("Procesando"),
    ENVIADO("Enviado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado"),
    DEVUELTO("Devuelto"),
    REEMBOLSADO("Reembolsado");

    private final String valor;

    EstadoPedido(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstadoPedido fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (EstadoPedido estado : EstadoPedido.values()) {
            if (estado.valor.equals(valor) || estado.name().equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pedido no válido: " + valor);
    }
}
