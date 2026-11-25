package com.example.DA2Back.pedidos;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.pedidos.negocio.state.EstadoAsignado;
import com.example.DA2Back.pedidos.negocio.state.EstadoCancelado;
import com.example.DA2Back.pedidos.negocio.state.EstadoCreado;
import com.example.DA2Back.pedidos.negocio.state.EstadoEnCamino;
import com.example.DA2Back.pedidos.negocio.state.EstadoEntregado;
import com.example.DA2Back.pedidos.negocio.state.EstadoListoParaRetirar;
import com.example.DA2Back.pedidos.negocio.state.EstadoRetirado;
import com.example.DA2Back.pedidos.negocio.state.IEstadoPedido;
import com.example.DA2Back.pedidos.negocio.state.ResolverEstadoPedido;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Patron State del Pedido (entidad central). Se prueba la matriz COMPLETA estado x operacion:
 * cada transicion valida cambia el estado y toda transicion invalida lanza
 * TransicionInvalidaPedidoException (que el GlobalExceptionHandler traduce a 409).
 */
class EstadoPedidoTransicionesTest {

    private enum Op {
        MARCAR_LISTO(EstadoPedido.LISTO_PARA_RETIRAR),
        ASIGNAR(EstadoPedido.ASIGNADO),
        MARCAR_RETIRADO(EstadoPedido.RETIRADO),
        INICIAR_VIAJE(EstadoPedido.EN_CAMINO),
        ENTREGAR(EstadoPedido.ENTREGADO),
        CANCELAR(EstadoPedido.CANCELADO);

        final EstadoPedido destino;

        Op(EstadoPedido destino) {
            this.destino = destino;
        }
    }

    /** Unicas transiciones permitidas por el ciclo de vida del pedido. */
    private static final Map<EstadoPedido, Set<Op>> PERMITIDAS = new EnumMap<>(EstadoPedido.class);

    static {
        PERMITIDAS.put(EstadoPedido.CREADO, EnumSet.of(Op.MARCAR_LISTO, Op.CANCELAR));
        PERMITIDAS.put(EstadoPedido.LISTO_PARA_RETIRAR, EnumSet.of(Op.ASIGNAR, Op.CANCELAR));
        PERMITIDAS.put(EstadoPedido.ASIGNADO, EnumSet.of(Op.MARCAR_RETIRADO, Op.CANCELAR));
        PERMITIDAS.put(EstadoPedido.RETIRADO, EnumSet.of(Op.INICIAR_VIAJE, Op.CANCELAR));
        PERMITIDAS.put(EstadoPedido.EN_CAMINO, EnumSet.of(Op.ENTREGAR));
        PERMITIDAS.put(EstadoPedido.ENTREGADO, EnumSet.noneOf(Op.class));
        PERMITIDAS.put(EstadoPedido.CANCELADO, EnumSet.noneOf(Op.class));
    }

    private final ResolverEstadoPedido resolver = new ResolverEstadoPedido(
            new EstadoCreado(), new EstadoListoParaRetirar(), new EstadoAsignado(),
            new EstadoRetirado(), new EstadoEnCamino(), new EstadoEntregado(), new EstadoCancelado());

    static Stream<Arguments> matrizCompleta() {
        return Stream.of(EstadoPedido.values())
                .flatMap(estado -> Stream.of(Op.values())
                        .map(op -> Arguments.of(estado, op, PERMITIDAS.get(estado).contains(op))));
    }

    @ParameterizedTest(name = "{0} + {1} -> permitida={2}")
    @MethodSource("matrizCompleta")
    @DisplayName("Matriz completa estado x operacion: valida cambia de estado, invalida lanza excepcion")
    void matriz(EstadoPedido estado, Op op, boolean permitida) {
        Pedido pedido = Pedido.builder().id(1L).estado(estado).build();
        Repartidor repartidor = disponible();
        IEstadoPedido state = resolver.resolver(estado);

        if (permitida) {
            ejecutar(state, pedido, repartidor, op);
            assertEquals(op.destino, pedido.getEstado());
            if (op == Op.ASIGNAR) {
                assertSame(repartidor, pedido.getRepartidor());
            }
        } else {
            assertThrows(TransicionInvalidaPedidoException.class, () -> ejecutar(state, pedido, repartidor, op));
            assertEquals(estado, pedido.getEstado(), "una transicion invalida no debe modificar el estado");
            assertNull(pedido.getRepartidor(), "ni asignar repartidor");
        }
    }

    @ParameterizedTest(name = "{0}: el mensaje indica estado actual y destino")
    @EnumSource(value = EstadoPedido.class, names = {"CREADO", "LISTO_PARA_RETIRAR", "ASIGNADO", "RETIRADO", "EN_CAMINO"})
    @DisplayName("Los estados no finales informan 'del estado X a Y' en la excepcion")
    void mensajeDeEstadosNoFinales(EstadoPedido estado) {
        for (Op op : Op.values()) {
            if (PERMITIDAS.get(estado).contains(op)) {
                continue;
            }
            Pedido pedido = Pedido.builder().estado(estado).build();
            TransicionInvalidaPedidoException ex = assertThrows(TransicionInvalidaPedidoException.class,
                    () -> ejecutar(resolver.resolver(estado), pedido, disponible(), op));
            assertTrue(ex.getMessage().contains(estado.name()), ex.getMessage());
            assertTrue(ex.getMessage().contains(op.destino.name()), ex.getMessage());
        }
    }

    @ParameterizedTest
    @EnumSource(value = EstadoPedido.class, names = {"ENTREGADO", "CANCELADO"})
    @DisplayName("Los estados finales rechazan TODA operacion con un mensaje especifico")
    void estadosFinalesRechazanTodo(EstadoPedido final_) {
        String esperado = final_ == EstadoPedido.ENTREGADO ? "entregado" : "cancelado";
        for (Op op : Op.values()) {
            Pedido pedido = Pedido.builder().estado(final_).build();
            if (op == Op.MARCAR_LISTO || op == Op.MARCAR_RETIRADO) {
                continue; // usan el mensaje generico de la interfaz (cubierto en la matriz)
            }
            TransicionInvalidaPedidoException ex = assertThrows(TransicionInvalidaPedidoException.class,
                    () -> ejecutar(resolver.resolver(final_), pedido, disponible(), op));
            assertTrue(ex.getMessage().toLowerCase().contains(esperado), ex.getMessage());
        }
    }

    // ───────────── asignacion: reglas del repartidor ─────────────

    @Test
    @DisplayName("Asignar a un repartidor INACTIVO se rechaza aunque figure disponible")
    void asignar_repartidorInactivo() {
        Pedido pedido = Pedido.builder().estado(EstadoPedido.LISTO_PARA_RETIRAR).build();
        Repartidor inactivo = Repartidor.builder().id(2L).estado(EstadoRepartidor.DISPONIBLE).activo(false).build();

        TransicionInvalidaPedidoException ex = assertThrows(TransicionInvalidaPedidoException.class,
                () -> new EstadoListoParaRetirar().asignarRepartidor(pedido, inactivo));

        assertTrue(ex.getMessage().contains("inactivo"));
        assertEquals(EstadoPedido.LISTO_PARA_RETIRAR, pedido.getEstado());
        assertNull(pedido.getRepartidor());
    }

    @ParameterizedTest
    @EnumSource(value = EstadoRepartidor.class, mode = EnumSource.Mode.EXCLUDE, names = "DISPONIBLE")
    @DisplayName("Asignar a un repartidor activo pero NO disponible se rechaza e informa su estado")
    void asignar_repartidorNoDisponible(EstadoRepartidor estadoRepartidor) {
        Pedido pedido = Pedido.builder().estado(EstadoPedido.LISTO_PARA_RETIRAR).build();
        Repartidor ocupado = Repartidor.builder().id(2L).estado(estadoRepartidor).activo(true).build();

        TransicionInvalidaPedidoException ex = assertThrows(TransicionInvalidaPedidoException.class,
                () -> new EstadoListoParaRetirar().asignarRepartidor(pedido, ocupado));

        assertTrue(ex.getMessage().contains(estadoRepartidor.name()));
        assertEquals(EstadoPedido.LISTO_PARA_RETIRAR, pedido.getEstado());
    }

    // ───────────── resolver ─────────────

    @Test
    @DisplayName("ResolverEstadoPedido devuelve la implementacion correcta para cada estado")
    void resolverMapeaCadaEstado() {
        assertInstanceOf(EstadoCreado.class, resolver.resolver(EstadoPedido.CREADO));
        assertInstanceOf(EstadoListoParaRetirar.class, resolver.resolver(EstadoPedido.LISTO_PARA_RETIRAR));
        assertInstanceOf(EstadoAsignado.class, resolver.resolver(EstadoPedido.ASIGNADO));
        assertInstanceOf(EstadoRetirado.class, resolver.resolver(EstadoPedido.RETIRADO));
        assertInstanceOf(EstadoEnCamino.class, resolver.resolver(EstadoPedido.EN_CAMINO));
        assertInstanceOf(EstadoEntregado.class, resolver.resolver(EstadoPedido.ENTREGADO));
        assertInstanceOf(EstadoCancelado.class, resolver.resolver(EstadoPedido.CANCELADO));
    }

    // ───────────── helpers ─────────────

    private static Repartidor disponible() {
        return Repartidor.builder().id(2L).estado(EstadoRepartidor.DISPONIBLE).activo(true).build();
    }

    private static void ejecutar(IEstadoPedido state, Pedido pedido, Repartidor repartidor, Op op) {
        switch (op) {
            case MARCAR_LISTO -> state.marcarListoParaRetirar(pedido);
            case ASIGNAR -> state.asignarRepartidor(pedido, repartidor);
            case MARCAR_RETIRADO -> state.marcarRetirado(pedido);
            case INICIAR_VIAJE -> state.iniciarViaje(pedido);
            case ENTREGAR -> state.entregar(pedido);
            case CANCELAR -> state.cancelar(pedido);
        }
    }
}
