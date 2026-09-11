package com.example.DA2Back.deposito.excepcion;

public class AsociacionInvalidaException extends RuntimeException {
    public AsociacionInvalidaException(String mensaje) {
        super(mensaje);
    }

}
