package com.example.DA2Back.Seguridad.negocio.State;

import com.example.DA2Back.Seguridad.dato.Usuario;

public interface IEstadoUsuario {
    void validar(Usuario usuario);
    void rechazar(Usuario usuario);
    void bloquear(Usuario usuario);
    void desbloquear(Usuario usuario);
}
