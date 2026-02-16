package com.web.prime_drip_club.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EstadoPago {
    PENDIENTE("Pendiente"),
    APROBADO("Aprobado"),
    RECHAZADO("Rechazado"),
    REEMBOLSADO("Reembolsado"),
    CANCELADO("Cancelado");

    private final String valor;

    EstadoPago(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstadoPago fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (EstadoPago estado : EstadoPago.values()) {
            if (estado.valor.equals(valor) || estado.name().equals(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pago no válido: " + valor);
    }
}
