package com.example.DA2Back.Seguridad.negocio.State;

import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResolverEstadoUsuario {

    private final EstadoEnEvaluacion estadoEnEvaluacion;
    private final EstadoValidado estadoValidado;
    private final EstadoBloqueado estadoBloqueado;
    private final EstadoRechazado estadoRechazado;

    public IEstadoUsuario resolver(EstadoUsuario estado) {
        return switch (estado) {
            case EN_EVALUACION -> estadoEnEvaluacion;
            case VALIDADO -> estadoValidado;
            case BLOQUEADO -> estadoBloqueado;
            case RECHAZADO -> estadoRechazado;
        };
    }
}