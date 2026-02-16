package com.web.prime_drip_club.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MetodoPago {
    TARJETA_CREDITO("Tarjeta de crédito"),
    TARJETA_DEBITO("Tarjeta de débito"),
    PSE("PSE"),
    TRANSFERENCIA("Transferencia"),
    EFECTIVO("Efectivo"),
    NEQUI("Nequi"),
    DAVIPLATA("Daviplata"),
    OTRO("Otro");

    private final String valor;

    MetodoPago(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static MetodoPago fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (MetodoPago metodo : MetodoPago.values()) {
            if (metodo.valor.equals(valor) || metodo.name().equals(valor)) {
                return metodo;
            }
        }
        throw new IllegalArgumentException("Método de pago no válido: " + valor);
    }
}
