package com.example.DA2Back.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;

@SpringBootTest
@ActiveProfiles("test")
class RepartidorTomarPedidoIntegrationTest {

    @Autowired
    private ServicioDePedidos servicioDePedidos;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RepartidorRepository repartidorRepository;

    @Autowired
    private PedidosRepository pedidosRepository;

    @Test
    @DisplayName("Dos repartidores no pueden tomar simultáneamente el mismo pedido")
    void dosRepartidoresNoPuedenTomarMismoPedido() throws Exception {
        Repartidor repartidorA = guardarRepartidor("concurrente-a");
        Repartidor repartidorB = guardarRepartidor("concurrente-b");
        Pedido pedido = guardarPedido("mismo-pedido", EstadoPedido.LISTO_PARA_RETIRAR, null);

        List<Boolean> resultados = ejecutarEnParalelo(
                () -> intentarAsignar(pedido.getId(), repartidorA.getId()),
                () -> intentarAsignar(pedido.getId(), repartidorB.getId())
        );

        Pedido actualizado = pedidosRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(1, resultados.stream().filter(Boolean::booleanValue).count());
        assertEquals(EstadoPedido.ASIGNADO, actualizado.getEstado());
        assertNotNull(actualizado.getRepartidor());

        long repartidoresEnEntrega = List.of(repartidorA, repartidorB).stream()
                .map(Repartidor::getId)
                .map(id -> repartidorRepository.findById(id).orElseThrow())
                .filter(repartidor -> repartidor.getEstado() == EstadoRepartidor.EN_ENTREGA)
                .count();
        assertEquals(1, repartidoresEnEntrega);
    }

    @Test
    @DisplayName("Un repartidor no puede tomar dos pedidos simultáneamente")
    void unRepartidorNoPuedeTomarDosPedidosSimultaneamente() throws Exception {
        Repartidor repartidor = guardarRepartidor("dos-pedidos");
        Pedido pedidoA = guardarPedido("pedido-a", EstadoPedido.LISTO_PARA_RETIRAR, null);
        Pedido pedidoB = guardarPedido("pedido-b", EstadoPedido.LISTO_PARA_RETIRAR, null);

        List<Boolean> resultados = ejecutarEnParalelo(
                () -> intentarAsignar(pedidoA.getId(), repartidor.getId()),
                () -> intentarAsignar(pedidoB.getId(), repartidor.getId())
        );

        Pedido actualizadoA = pedidosRepository.findById(pedidoA.getId()).orElseThrow();
        Pedido actualizadoB = pedidosRepository.findById(pedidoB.getId()).orElseThrow();

        assertEquals(1, resultados.stream().filter(Boolean::booleanValue).count());
        long pedidosAsignados = List.of(actualizadoA, actualizadoB).stream()
                .filter(pedido -> pedido.getEstado() == EstadoPedido.ASIGNADO)
                .filter(pedido -> pedido.getRepartidor() != null)
                .count();
        assertEquals(1, pedidosAsignados);

        Repartidor actualizado = repartidorRepository.findById(repartidor.getId()).orElseThrow();
        assertEquals(EstadoRepartidor.EN_ENTREGA, actualizado.getEstado());
    }

    private boolean intentarAsignar(Long pedidoId, Long repartidorId) {
        try {
            servicioDePedidos.asignarRepartidor(pedidoId, repartidorId);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private List<Boolean> ejecutarEnParalelo(Callable<Boolean> primera, Callable<Boolean> segunda) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        try {
            var futuroA = executor.submit(() -> {
                inicio.await();
                return primera.call();
            });
            var futuroB = executor.submit(() -> {
                inicio.await();
                return segunda.call();
            });

            inicio.countDown();
            return List.of(
                    futuroA.get(10, TimeUnit.SECONDS),
                    futuroB.get(10, TimeUnit.SECONDS)
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private Repartidor guardarRepartidor(String sufijo) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email("repartidor." + sufijo + "." + UUID.randomUUID() + "@test.com")
                .password("test")
                .nombre("Repartidor")
                .apellido(sufijo)
                .DNI("DNI-" + UUID.randomUUID())
                .telefono("1100000000")
                .vehiculo("Moto")
                .rol(Rol.REPARTIDOR)
                .estado(EstadoUsuario.VALIDADO)
                .build());

        return repartidorRepository.save(Repartidor.builder()
                .usuarioId(usuario.getId())
                .patente("PAT-" + UUID.randomUUID())
                .zona("Centro")
                .estado(EstadoRepartidor.DISPONIBLE)
                .activo(true)
                .build());
    }

    private Pedido guardarPedido(String sufijo, EstadoPedido estado, Repartidor repartidor) {
        return pedidosRepository.save(Pedido.builder()
                .comercioId(1L)
                .depositoId(1L)
                .direccionOrigen("Origen " + sufijo)
                .direccionDestino("Destino " + sufijo)
                .fechaCreacion(LocalDateTime.now())
                .estado(estado)
                .repartidor(repartidor)
                .build());
    }
}
