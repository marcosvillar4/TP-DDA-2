package com.example.DA2Back.Seguridad.negocio.State;

import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;

@Component
public class EstadoEnEvaluacion implements IEstadoUsuario {

    @Override
    public void validar(Usuario usuario) {
        usuario.setEstado(EstadoUsuario.VALIDADO);
    }

    @Override
    public void rechazar(Usuario usuario) {
        usuario.setEstado(EstadoUsuario.RECHAZADO);
    }

    @Override
    public void bloquear(Usuario usuario) {
        throw new TransicionInvalidaException("No se puede bloquear un usuario en evaluación");
    }

    @Override
    public void desbloquear(Usuario usuario) {
        throw new TransicionInvalidaException("El usuario no está bloqueado");
    }
}