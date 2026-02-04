package com.web.prime_drip_club.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SexoProducto {
    HOMBRE("Hombre"),
    MUJER("Mujer"),
    NIÑO("Niño"),
    UNISEX("Unisex");

    private final String valor;

    SexoProducto(String valor) {
        this.valor = valor;
    }

    /**
     * Obtiene el valor de display del sexo
     * 
     * @return valor del sexo para mostrar
     */
    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Convierte un String a un SexoProducto
     * 
     * @param valor el valor a convertir
     * @return SexoProducto correspondiente
     * @throws IllegalArgumentException si el valor no es válido
     */
    @JsonCreator
    public static SexoProducto fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (SexoProducto sexo : SexoProducto.values()) {
            if (sexo.valor.equals(valor)) {
                return sexo;
            }
        }
        throw new IllegalArgumentException("Sexo no válido: " + valor);
    }
}