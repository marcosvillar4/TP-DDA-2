package com.example.DA2Back.pago.dato;

public enum MetodoPago {
    TARJETA_CREDITO,
    TARJETA_DEBITO,
    TRANSFERENCIA;

    public boolean esTarjeta() {
        return this == TARJETA_CREDITO || this == TARJETA_DEBITO;
    }
}
