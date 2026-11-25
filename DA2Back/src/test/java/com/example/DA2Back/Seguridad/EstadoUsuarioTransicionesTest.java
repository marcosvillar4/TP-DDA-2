package com.example.DA2Back.Seguridad;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;
import com.example.DA2Back.Seguridad.negocio.State.EstadoBloqueado;
import com.example.DA2Back.Seguridad.negocio.State.EstadoEnEvaluacion;
import com.example.DA2Back.Seguridad.negocio.State.EstadoRechazado;
import com.example.DA2Back.Seguridad.negocio.State.EstadoValidado;
import com.example.DA2Back.Seguridad.negocio.State.IEstadoUsuario;
import com.example.DA2Back.Seguridad.negocio.State.ResolverEstadoUsuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Patron State del Usuario: matriz completa estado x operacion (4 x 4). */
class EstadoUsuarioTransicionesTest {

    private enum Op {
        VALIDAR(EstadoUsuario.VALIDADO),
        RECHAZAR(EstadoUsuario.RECHAZADO),
        BLOQUEAR(EstadoUsuario.BLOQUEADO),
        DESBLOQUEAR(EstadoUsuario.VALIDADO);

        final EstadoUsuario destino;

        Op(EstadoUsuario destino) {
            this.destino = destino;
        }
    }

    private static final Map<EstadoUsuario, Set<Op>> PERMITIDAS = new EnumMap<>(EstadoUsuario.class);

    static {
        PERMITIDAS.put(EstadoUsuario.EN_EVALUACION, EnumSet.of(Op.VALIDAR, Op.RECHAZAR));
        PERMITIDAS.put(EstadoUsuario.VALIDADO, EnumSet.of(Op.BLOQUEAR));
        PERMITIDAS.put(EstadoUsuario.BLOQUEADO, EnumSet.of(Op.DESBLOQUEAR));
        PERMITIDAS.put(EstadoUsuario.RECHAZADO, EnumSet.noneOf(Op.class));
    }

    private final ResolverEstadoUsuario resolver = new ResolverEstadoUsuario(
            new EstadoEnEvaluacion(), new EstadoValidado(), new EstadoBloqueado(), new EstadoRechazado());

    static Stream<Arguments> matriz() {
        return Stream.of(EstadoUsuario.values())
                .flatMap(estado -> Stream.of(Op.values())
                        .map(op -> Arguments.of(estado, op, PERMITIDAS.get(estado).contains(op))));
    }

    @ParameterizedTest(name = "{0} + {1} -> permitida={2}")
    @MethodSource("matriz")
    @DisplayName("Matriz completa: transicion valida cambia el estado; invalida lanza TransicionInvalidaException")
    void matrizCompleta(EstadoUsuario estado, Op op, boolean permitida) {
        Usuario usuario = Usuario.builder().id(1L).estado(estado).build();
        IEstadoUsuario state = resolver.resolver(estado);

        if (permitida) {
            ejecutar(state, usuario, op);
            assertEquals(op.destino, usuario.getEstado());
        } else {
            TransicionInvalidaException ex =
                    assertThrows(TransicionInvalidaException.class, () -> ejecutar(state, usuario, op));
            assertNotNull(ex.getMessage());
            assertEquals(estado, usuario.getEstado(), "una transicion invalida no debe cambiar el estado");
        }
    }

    @Test
    @DisplayName("ResolverEstadoUsuario devuelve la implementacion de cada estado")
    void resolverMapea() {
        assertInstanceOf(EstadoEnEvaluacion.class, resolver.resolver(EstadoUsuario.EN_EVALUACION));
        assertInstanceOf(EstadoValidado.class, resolver.resolver(EstadoUsuario.VALIDADO));
        assertInstanceOf(EstadoBloqueado.class, resolver.resolver(EstadoUsuario.BLOQUEADO));
        assertInstanceOf(EstadoRechazado.class, resolver.resolver(EstadoUsuario.RECHAZADO));
    }

    private static void ejecutar(IEstadoUsuario state, Usuario usuario, Op op) {
        switch (op) {
            case VALIDAR -> state.validar(usuario);
            case RECHAZAR -> state.rechazar(usuario);
            case BLOQUEAR -> state.bloquear(usuario);
            case DESBLOQUEAR -> state.desbloquear(usuario);
        }
    }
}
