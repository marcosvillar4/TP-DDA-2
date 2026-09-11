package com.example.DA2Back.Seguridad.negocio.State;

import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;

@Component
public class EstadoBloqueado implements IEstadoUsuario {

    @Override
    public void validar(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario está bloqueado");
    }

    @Override
    public void rechazar(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario está bloqueado");
    }

    @Override
    public void bloquear(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario ya está bloqueado");
    }

    @Override
    public void desbloquear(Usuario usuario) {
        usuario.setEstado(EstadoUsuario.VALIDADO);
    }
}
