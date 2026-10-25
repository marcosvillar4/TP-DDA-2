package com.example.DA2Back.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;

/**
 * Proceso de negocio de punta a punta con mensajeria asincronica REAL (ActiveMQ):
 *
 *   Comercio crea el pedido -> queda LISTO_PARA_RETIRAR -> (commit) -> mensaje en la cola
 *   -> consumidor asigna un repartidor -> el repartidor retira, viaja y entrega.
 *
 * Usa un broker ActiveMQ en memoria (vm://) para no requerir nada instalado. NO es @Transactional
 * a proposito: el mensaje se publica despues del commit, y el consumidor corre en otro hilo.
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:asignacionasync;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.mensajeria.habilitada=true",
        "app.mensajeria.broker-embebido=false",
        "app.mensajeria.cola-pedidos-listos=test.pedidos.listos",
        "spring.activemq.broker-url=vm://asignacion-test?broker.persistent=false&broker.useJmx=false"
                + "&jms.redeliveryPolicy.maximumRedeliveries=30"
                + "&jms.redeliveryPolicy.initialRedeliveryDelay=300"
                + "&jms.redeliveryPolicy.redeliveryDelay=300"
})
@ActiveProfiles("test")
class PedidoAsignacionAsincronaIntegrationTest {

    private static final long ESPERA_MAX_MS = 15_000;

    @Autowired private ServicioDePedidos servicioDePedidos;
    @Autowired private IRepartidorService repartidorService;
    @Autowired private PedidosRepository pedidosRepository;
    @Autowired private RepartidorRepository repartidorRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ComercioRepository comercioRepository;
    @Autowired private DepositoRepository depositoRepository;
    @Autowired private JdbcTemplate jdbc;

    private final Usuario admin = Usuario.builder().id(1L).rol(Rol.ADMIN).estado(EstadoUsuario.VALIDADO).build();
    private Long comercioId;
    private Long depositoId;

    @BeforeEach
    void preparar() {
        // Aislamiento: ningun repartidor de otros tests puede competir por el pedido.
        jdbc.update("update repartidores set estado = 'NO_DISPONIBLE'");

        String sufijo = UUID.randomUUID().toString();
        Comercio comercio = comercioRepository.save(Comercio.builder()
                .usuarioId(System.nanoTime())
                .nombreComercial("Comercio " + sufijo).razonSocial("Razon " + sufijo)
                .direccion("Av. Siempre Viva 123").CUIT("CUIT-" + sufijo)
                .telefono("1100000000").email(sufijo + "@test.com").build());
        Deposito deposito = new Deposito();
        deposito.setNombre("Deposito " + sufijo);
        deposito.setDireccion("Calle Falsa 456");
        deposito.setUsuarioId(System.nanoTime());
        deposito.setComercio(comercio);
        comercioId = comercio.getId();
        depositoId = depositoRepository.save(deposito).getId();
    }

    @Test
    @DisplayName("Comercio crea el pedido, la cola asigna un repartidor y el repartidor lo entrega")
    void procesoCompletoDePedidoHastaLaEntrega() {
        Repartidor repartidor = guardarRepartidor();
        Long pedidoId = crearPedidoListoParaRetirar();

        // El comercio ya recibio su respuesta; la asignacion ocurre de forma asincronica.
        esperarHasta(() -> estadoDe(pedidoId) == EstadoPedido.ASIGNADO);

        Pedido asignado = pedidosRepository.findById(pedidoId).orElseThrow();
        assertNotNull(asignado.getRepartidor());
        assertEquals(repartidor.getId(), asignado.getRepartidor().getId());
        assertEquals(EstadoRepartidor.EN_ENTREGA,
                repartidorRepository.findById(repartidor.getId()).orElseThrow().getEstado());

        // El repartidor completa la entrega con las operaciones que ya existian.
        Long usuarioId = repartidor.getUsuarioId();
        repartidorService.retirarPedidoParaUsuario(usuarioId, pedidoId);
        repartidorService.iniciarViajeParaUsuario(usuarioId, pedidoId);
        repartidorService.entregarPedidoParaUsuario(usuarioId, pedidoId);

        assertEquals(EstadoPedido.ENTREGADO, estadoDe(pedidoId));
        assertEquals(EstadoRepartidor.DISPONIBLE,
                repartidorRepository.findById(repartidor.getId()).orElseThrow().getEstado());
        assertEquals(List.of("CREADO", "LISTO_PARA_RETIRAR", "ASIGNADO", "RETIRADO", "EN_CAMINO", "ENTREGADO"),
                jdbc.queryForList("select estado from historial_estado_pedido where pedido_id = ? order by id",
                        String.class, pedidoId));
    }

    @Test
    @DisplayName("Sin repartidores el mensaje se reentrega y se asigna apenas aparece uno disponible")
    void sinRepartidorDisponibleReintentaHastaQueAparezcaUno() throws InterruptedException {
        Long pedidoId = crearPedidoListoParaRetirar();

        Thread.sleep(1_500); // varios intentos de entrega fallidos
        assertEquals(EstadoPedido.LISTO_PARA_RETIRAR, estadoDe(pedidoId));
        assertNull(pedidosRepository.findById(pedidoId).orElseThrow().getRepartidor());

        Repartidor repartidor = guardarRepartidor();

        esperarHasta(() -> estadoDe(pedidoId) == EstadoPedido.ASIGNADO);
        assertEquals(repartidor.getId(),
                pedidosRepository.findById(pedidoId).orElseThrow().getRepartidor().getId());
    }

    // ------------------------------------------------------------------ helpers

    private Long crearPedidoListoParaRetirar() {
        CrearPedidoDTO dto = new CrearPedidoDTO();
        dto.setComercioId(comercioId);
        dto.setDepositoId(depositoId);
        dto.setDireccionDestino("Destino " + UUID.randomUUID());
        Long id = servicioDePedidos.crearPedido(dto, admin).getId();
        servicioDePedidos.marcarListoParaRetirar(id, admin);   // dispara el mensaje (tras el commit)
        return id;
    }

    private EstadoPedido estadoDe(Long pedidoId) {
        return pedidosRepository.findById(pedidoId).orElseThrow().getEstado();
    }

    private void esperarHasta(BooleanSupplier condicion) {
        long limite = System.currentTimeMillis() + ESPERA_MAX_MS;
        while (System.currentTimeMillis() < limite) {
            if (condicion.getAsBoolean()) return;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Espera interrumpida", e);
            }
        }
        throw new AssertionError("La condicion no se cumplio en " + ESPERA_MAX_MS + " ms");
    }

    private Repartidor guardarRepartidor() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .email("repartidor." + UUID.randomUUID() + "@test.com")
                .password("test").nombre("Repartidor").apellido("Async")
                .DNI("DNI-" + UUID.randomUUID()).telefono("1100000000").vehiculo("Moto")
                .rol(Rol.REPARTIDOR).estado(EstadoUsuario.VALIDADO).build());
        return repartidorRepository.save(Repartidor.builder()
                .usuarioId(usuario.getId())
                .patente("PAT-" + UUID.randomUUID())
                .zona("Centro")
                .estado(EstadoRepartidor.DISPONIBLE)
                .activo(true).build());
    }
}
