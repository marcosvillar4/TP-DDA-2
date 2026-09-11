package com.example.DA2Back.Seguridad.negocio.State;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;

public class EstadoValidado implements IEstadoUsuario {

    @Override
    public void validar(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario ya está validado");
    }

    @Override
    public void rechazar(Usuario usuario) {
        throw new TransicionInvalidaException("No se puede rechazar un usuario ya validado");
    }

    @Override
    public void bloquear(Usuario usuario) {
        usuario.setEstado(EstadoUsuario.BLOQUEADO);
    }

    @Override
    public void desbloquear(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario no está bloqueado");
    }
}
