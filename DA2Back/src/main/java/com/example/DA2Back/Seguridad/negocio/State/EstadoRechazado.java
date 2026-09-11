package com.example.DA2Back.Seguridad.negocio.State;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;

public class EstadoRechazado implements IEstadoUsuario {

    @Override
    public void validar(Usuario usuario) {
        throw new TransicionInvalidaException("Un usuario rechazado no puede validarse");
    }

    @Override
    public void rechazar(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario ya está rechazado");
    }

    @Override
    public void bloquear(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario está rechazado");
    }

    @Override
    public void desbloquear(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario está rechazado");
    }
}
