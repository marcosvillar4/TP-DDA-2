package com.example.DA2Back.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedidoRepository;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;

@SpringBootTest
@ActiveProfiles("test")
class RepartidorCicloEntregaIntegrationTest {

    @Autowired
    private IRepartidorService repartidorService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RepartidorRepository repartidorRepository;

    @Autowired
    private PedidosRepository pedidosRepository;

    @Autowired
    private HistorialEstadoPedidoRepository historialRepository;

    @Test
    @DisplayName("Repartidor retira pedido propio ASIGNADO")
    void retirarPedidoPropio() {
        Repartidor repartidor = guardarRepartidor("retirar-propio", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("retirar-propio", EstadoPedido.ASIGNADO, repartidor);

        var response = repartidorService.retirarPedidoParaUsuario(repartidor.getUsuarioId(), pedido.getId());

        Pedido actualizado = pedidosRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.RETIRADO, response.getEstado());
        assertEquals(EstadoPedido.RETIRADO, actualizado.getEstado());
        assertEquals(EstadoRepartidor.EN_ENTREGA, repartidorRepository.findById(repartidor.getId()).orElseThrow().getEstado());
        assertHistorial(pedido.getId(), EstadoPedido.RETIRADO, 1);
    }

    @Test
    @DisplayName("Repartidor no puede retirar pedido ajeno")
    void rechazaRetiroAjeno() {
        Repartidor repartidorAutenticado = guardarRepartidor("retirar-ajeno-a", true, EstadoRepartidor.EN_ENTREGA);
        Repartidor repartidorAsignado = guardarRepartidor("retirar-ajeno-b", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("retirar-ajeno", EstadoPedido.ASIGNADO, repartidorAsignado);

        assertThrows(AccessDeniedException.class,
                () -> repartidorService.retirarPedidoParaUsuario(repartidorAutenticado.getUsuarioId(), pedido.getId()));
        assertEquals(EstadoPedido.ASIGNADO, pedidosRepository.findById(pedido.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Repartidor no puede retirar pedido en estado incorrecto")
    void rechazaRetiroEstadoIncorrecto() {
        Repartidor repartidor = guardarRepartidor("retirar-estado", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("retirar-estado", EstadoPedido.RETIRADO, repartidor);

        assertThrows(TransicionInvalidaPedidoException.class,
                () -> repartidorService.retirarPedidoParaUsuario(repartidor.getUsuarioId(), pedido.getId()));
    }

    @Test
    @DisplayName("Repartidor inicia viaje de pedido propio RETIRADO")
    void iniciarViajePropio() {
        Repartidor repartidor = guardarRepartidor("viaje-propio", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("viaje-propio", EstadoPedido.RETIRADO, repartidor);

        var response = repartidorService.iniciarViajeParaUsuario(repartidor.getUsuarioId(), pedido.getId());

        Pedido actualizado = pedidosRepository.findById(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.EN_CAMINO, response.getEstado());
        assertEquals(EstadoPedido.EN_CAMINO, actualizado.getEstado());
        assertEquals(EstadoRepartidor.EN_ENTREGA, repartidorRepository.findById(repartidor.getId()).orElseThrow().getEstado());
        assertHistorial(pedido.getId(), EstadoPedido.EN_CAMINO, 1);
    }

    @Test
    @DisplayName("Repartidor no puede iniciar viaje de pedido ajeno")
    void rechazaInicioViajeAjeno() {
        Repartidor repartidorAutenticado = guardarRepartidor("viaje-ajeno-a", true, EstadoRepartidor.EN_ENTREGA);
        Repartidor repartidorAsignado = guardarRepartidor("viaje-ajeno-b", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("viaje-ajeno", EstadoPedido.RETIRADO, repartidorAsignado);

        assertThrows(AccessDeniedException.class,
                () -> repartidorService.iniciarViajeParaUsuario(repartidorAutenticado.getUsuarioId(), pedido.getId()));
        assertEquals(EstadoPedido.RETIRADO, pedidosRepository.findById(pedido.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Repartidor no puede iniciar viaje en estado incorrecto")
    void rechazaInicioViajeEstadoIncorrecto() {
        Repartidor repartidor = guardarRepartidor("viaje-estado", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("viaje-estado", EstadoPedido.ASIGNADO, repartidor);

        assertThrows(TransicionInvalidaPedidoException.class,
                () -> repartidorService.iniciarViajeParaUsuario(repartidor.getUsuarioId(), pedido.getId()));
    }

    @Test
    @DisplayName("Repartidor entrega pedido propio EN_CAMINO y vuelve a DISPONIBLE")
    void entregarPedidoPropio() {
        Repartidor repartidor = guardarRepartidor("entregar-propio", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("entregar-propio", EstadoPedido.EN_CAMINO, repartidor);

        var response = repartidorService.entregarPedidoParaUsuario(repartidor.getUsuarioId(), pedido.getId());

        Pedido actualizado = pedidosRepository.findById(pedido.getId()).orElseThrow();
        Repartidor repartidorActualizado = repartidorRepository.findById(repartidor.getId()).orElseThrow();
        assertEquals(EstadoPedido.ENTREGADO, response.getEstado());
        assertEquals(EstadoPedido.ENTREGADO, actualizado.getEstado());
        assertEquals(repartidor.getId(), actualizado.getRepartidor().getId());
        assertEquals(EstadoRepartidor.DISPONIBLE, repartidorActualizado.getEstado());
        assertHistorial(pedido.getId(), EstadoPedido.ENTREGADO, 1);
    }

    @Test
    @DisplayName("Repartidor no puede entregar pedido ajeno")
    void rechazaEntregaAjena() {
        Repartidor repartidorAutenticado = guardarRepartidor("entrega-ajena-a", true, EstadoRepartidor.EN_ENTREGA);
        Repartidor repartidorAsignado = guardarRepartidor("entrega-ajena-b", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("entrega-ajena", EstadoPedido.EN_CAMINO, repartidorAsignado);

        assertThrows(AccessDeniedException.class,
                () -> repartidorService.entregarPedidoParaUsuario(repartidorAutenticado.getUsuarioId(), pedido.getId()));
        assertEquals(EstadoPedido.EN_CAMINO, pedidosRepository.findById(pedido.getId()).orElseThrow().getEstado());
    }

    @Test
    @DisplayName("Repartidor no puede entregar pedido en estado incorrecto")
    void rechazaEntregaEstadoIncorrecto() {
        Repartidor repartidor = guardarRepartidor("entrega-estado", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("entrega-estado", EstadoPedido.RETIRADO, repartidor);

        assertThrows(TransicionInvalidaPedidoException.class,
                () -> repartidorService.entregarPedidoParaUsuario(repartidor.getUsuarioId(), pedido.getId()));
    }

    @Test
    @DisplayName("Repartidor inactivo no puede operar pedido propio")
    void repartidorInactivoNoPuedeOperar() {
        Repartidor repartidor = guardarRepartidor("inactivo", false, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("inactivo", EstadoPedido.ASIGNADO, repartidor);

        assertThrows(IllegalStateException.class,
                () -> repartidorService.retirarPedidoParaUsuario(repartidor.getUsuarioId(), pedido.getId()));
    }

    @Test
    @DisplayName("Usuario sin perfil operativo recibe error controlado")
    void usuarioSinPerfilNoPuedeOperar() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email("sin.perfil." + UUID.randomUUID() + "@test.com")
                .password("test")
                .nombre("Sin")
                .apellido("Perfil")
                .DNI("DNI-" + UUID.randomUUID())
                .telefono("1100000000")
                .vehiculo("Moto")
                .rol(Rol.REPARTIDOR)
                .estado(EstadoUsuario.VALIDADO)
                .build());

        assertThrows(java.util.NoSuchElementException.class,
                () -> repartidorService.retirarPedidoParaUsuario(usuario.getId(), 1L));
    }

    @Test
    @DisplayName("Dos entregas concurrentes no duplican transición ni historial")
    void entregaConcurrenteNoDuplicaTransicion() throws Exception {
        Repartidor repartidor = guardarRepartidor("entrega-concurrente", true, EstadoRepartidor.EN_ENTREGA);
        Pedido pedido = guardarPedido("entrega-concurrente", EstadoPedido.EN_CAMINO, repartidor);

        List<Boolean> resultados = ejecutarEnParalelo(
                () -> intentarEntregar(repartidor.getUsuarioId(), pedido.getId()),
                () -> intentarEntregar(repartidor.getUsuarioId(), pedido.getId()));

        Pedido actualizado = pedidosRepository.findById(pedido.getId()).orElseThrow();
        Repartidor repartidorActualizado = repartidorRepository.findById(repartidor.getId()).orElseThrow();
        assertEquals(1, resultados.stream().filter(Boolean::booleanValue).count());
        assertEquals(EstadoPedido.ENTREGADO, actualizado.getEstado());
        assertEquals(EstadoRepartidor.DISPONIBLE, repartidorActualizado.getEstado());
        assertHistorial(pedido.getId(), EstadoPedido.ENTREGADO, 1);
    }

    private boolean intentarEntregar(Long usuarioId, Long pedidoId) {
        try {
            repartidorService.entregarPedidoParaUsuario(usuarioId, pedidoId);
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

    private void assertHistorial(Long pedidoId, EstadoPedido estado, int cantidad) {
        long ocurrencias = historialRepository.findByPedidoIdOrderByFechaHoraDesc(pedidoId).stream()
                .filter(historial -> historial.getEstado() == estado)
                .count();
        assertEquals(cantidad, ocurrencias);
    }

    private Repartidor guardarRepartidor(String sufijo, boolean activo, EstadoRepartidor estado) {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email("repartidor.ciclo." + sufijo + "." + UUID.randomUUID() + "@test.com")
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
                .estado(estado)
                .activo(activo)
                .build());
    }

    private Pedido guardarPedido(String sufijo, EstadoPedido estado, Repartidor repartidor) {
        Pedido pedido = pedidosRepository.save(Pedido.builder()
                .comercioId(1L)
                .depositoId(1L)
                .direccionOrigen("Origen " + sufijo)
                .direccionDestino("Destino " + sufijo)
                .fechaCreacion(LocalDateTime.now())
                .estado(estado)
                .repartidor(repartidor)
                .build());

        assertNotNull(pedido.getId());
        assertSame(repartidor, pedido.getRepartidor());
        return pedido;
    }
}
