package com.web.prime_drip_club.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EtiquetaProducto {
    AGOTADO("Agotado"),
    NUEVO("Nuevo"),
    OFERTA("Oferta"),
    DESTACADO("Destacado"),
    ULTIMAS_UNIDADES("Últimas unidades");

    private final String valor;

    EtiquetaProducto(String valor) {
        this.valor = valor;
    }

    /**
     * obtiene el valor de la etiqueta
     * @return valor de la etiqueta
     */
    @JsonValue
    public String getValor() {
        return valor;
    }

    /**
     * Convierte un String a una EtiquetaProducto
     * @param valor
     * @return etiqueta, null si no existe, IllegalArgumentException si el valor no es válido
     */
    @JsonCreator
    public static EtiquetaProducto fromValor(String valor) {
        if (valor == null) {
            return null;
        }
        for (EtiquetaProducto etiqueta : EtiquetaProducto.values()) {
            if (etiqueta.valor.equals(valor)) {
                return etiqueta;
            }
        }
        throw new IllegalArgumentException("Etiqueta no válida: " + valor);
    }
}
