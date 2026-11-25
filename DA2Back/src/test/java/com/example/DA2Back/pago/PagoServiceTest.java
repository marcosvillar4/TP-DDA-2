package com.example.DA2Back.pago;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.pago.dato.EstadoPago;
import com.example.DA2Back.pago.dato.MetodoPago;
import com.example.DA2Back.pago.dato.Pago;
import com.example.DA2Back.pago.dato.PagoRepository;
import com.example.DA2Back.pago.dto.PagarPedidoDTO;
import com.example.DA2Back.pago.dto.PagoResponseDTO;
import com.example.DA2Back.pago.excepcion.PagoRechazadoException;
import com.example.DA2Back.pago.negocio.PagoRegistrador;
import com.example.DA2Back.pago.negocio.PagoService;
import com.example.DA2Back.pago.negocio.PasarelaPago;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PagoServiceTest {

    private static final String TARJETA_OK = "4242424242424242";
    private static final String CBU_OK = "0170099220000000123453";

    @Mock private PagoRepository pagoRepository;
    @Mock private PedidosRepository pedidosRepository;
    @Mock private ComercioRepository comercioRepository;
    @Mock private RepartidorRepository repartidorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PagoRegistrador registrador;
    @Mock private PasarelaPago pasarela;

    @InjectMocks
    private PagoService service;

    private Usuario usuarioComercio;
    private Comercio comercio;

    @BeforeEach
    void setUp() {
        usuarioComercio = usuario(10L, Rol.COMERCIO, EstadoUsuario.VALIDADO);
        comercio = Comercio.builder().id(1L).nombreComercial("Urban Shoes").usuarioId(10L).build();
        when(comercioRepository.findByUsuarioId(10L)).thenReturn(Optional.of(comercio));
    }

    // ───────────── resolverComercio / autorizacion ─────────────

    @Test
    @DisplayName("Listar pagos: usuario null, sin id, deshabilitado o con otro rol -> AccessDenied")
    void listar_usuarioNoAutorizado_denegado() {
        assertThrows(AccessDeniedException.class, () -> service.listarPendientesComercio(null));
        assertThrows(AccessDeniedException.class,
                () -> service.listarPendientesComercio(usuario(null, Rol.COMERCIO, EstadoUsuario.VALIDADO)));
        assertThrows(AccessDeniedException.class,
                () -> service.listarPendientesComercio(usuario(10L, Rol.COMERCIO, EstadoUsuario.BLOQUEADO)));
        assertThrows(AccessDeniedException.class,
                () -> service.listarPendientesComercio(usuario(10L, Rol.REPARTIDOR, EstadoUsuario.VALIDADO)));
    }

    @Test
    @DisplayName("Listar pagos: comercio sin perfil asociado -> AccessDenied")
    void listar_sinComercioAsociado_denegado() {
        when(comercioRepository.findByUsuarioId(10L)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> service.listarPendientesComercio(usuarioComercio));
    }

    // ───────────── listarComercio + sincronizar ─────────────

    @Test
    @DisplayName("Listar por estado: crea el pago faltante de un pedido con costo y devuelve el DTO armado")
    void listarComercio_creaPagoFaltanteYDevuelveDto() {
        Pedido conCosto = pedido(100L, EstadoPedido.CREADO, "1500.00", null);
        when(pedidosRepository.findByComercioId(1L)).thenReturn(List.of(conCosto));
        when(pagoRepository.findByPedidoIdIn(anyCollection())).thenReturn(List.of());
        Pago pendiente = pago(5L, 100L, "1500.00", EstadoPago.PENDIENTE);
        when(pagoRepository.findByComercioIdAndEstadoOrderByFechaCreacionDesc(1L, EstadoPago.PENDIENTE))
                .thenReturn(List.of(pendiente));
        stubLookupsDto(conCosto);

        List<PagoResponseDTO> res = service.listarPendientesComercio(usuarioComercio);

        verify(registrador).asegurarPendiente(conCosto);
        assertEquals(1, res.size());
        assertEquals(5L, res.get(0).getId());
        assertEquals("Urban Shoes", res.get(0).getComercioNombre());
        assertEquals(EstadoPedido.CREADO, res.get(0).getPedidoEstado());
        assertNull(res.get(0).getRepartidorId());
        assertNull(res.get(0).getRepartidorNombre());
    }

    @Test
    @DisplayName("Listar sin filtro de estado usa la consulta de todos los pagos del comercio")
    void listarComercio_sinEstado_listaTodos() {
        when(pedidosRepository.findByComercioId(1L)).thenReturn(List.of());
        when(pagoRepository.findByComercioIdOrderByFechaCreacionDesc(1L)).thenReturn(List.of());

        assertTrue(service.listarComercio(usuarioComercio, null).isEmpty());

        verify(pagoRepository).findByComercioIdOrderByFechaCreacionDesc(1L);
        verify(pagoRepository, never()).findByPedidoIdIn(anyCollection());
    }

    @Test
    @DisplayName("Sincronizar: no crea pago para pedidos sin costo, con costo cero o cancelados")
    void sincronizar_pedidosSinCostoOCancelados_noCreaPago() {
        Pedido sinCosto = pedido(101L, EstadoPedido.CREADO, null, null);
        Pedido costoCero = pedido(102L, EstadoPedido.CREADO, "0", null);
        Pedido cancelado = pedido(103L, EstadoPedido.CANCELADO, "900", null);
        when(pedidosRepository.findByComercioId(1L)).thenReturn(List.of(sinCosto, costoCero, cancelado));
        when(pagoRepository.findByPedidoIdIn(anyCollection())).thenReturn(List.of());
        when(pagoRepository.findByComercioIdAndEstadoOrderByFechaCreacionDesc(1L, EstadoPago.PENDIENTE))
                .thenReturn(List.of());

        service.listarPendientesComercio(usuarioComercio);

        verify(registrador, never()).asegurarPendiente(any());
    }

    @Test
    @DisplayName("Sincronizar: si otro request creo el pago en simultaneo, la colision se ignora")
    void sincronizar_colisionConcurrente_seIgnora() {
        Pedido pedido = pedido(100L, EstadoPedido.CREADO, "700", null);
        when(pedidosRepository.findByComercioId(1L)).thenReturn(List.of(pedido));
        when(pagoRepository.findByPedidoIdIn(anyCollection())).thenReturn(List.of());
        doThrow(new DataIntegrityViolationException("dup")).when(registrador).asegurarPendiente(pedido);
        when(pagoRepository.findByComercioIdAndEstadoOrderByFechaCreacionDesc(1L, EstadoPago.PENDIENTE))
                .thenReturn(List.of());

        assertTrue(service.listarPendientesComercio(usuarioComercio).isEmpty());
    }

    @Test
    @DisplayName("Sincronizar: anula el pago PENDIENTE de un pedido cancelado; no toca uno ya PAGADO")
    void sincronizar_pedidoCancelado_anulaSoloPendientes() {
        Pedido cancelado1 = pedido(100L, EstadoPedido.CANCELADO, "500", null);
        Pedido cancelado2 = pedido(101L, EstadoPedido.CANCELADO, "500", null);
        Pedido activo = pedido(102L, EstadoPedido.CREADO, "500", null);
        Pago pendiente = pago(1L, 100L, "500", EstadoPago.PENDIENTE);
        Pago pagado = pago(2L, 101L, "500", EstadoPago.PAGADO);
        Pago delActivo = pago(3L, 102L, "500", EstadoPago.PENDIENTE);
        when(pedidosRepository.findByComercioId(1L)).thenReturn(List.of(cancelado1, cancelado2, activo));
        when(pagoRepository.findByPedidoIdIn(anyCollection())).thenReturn(List.of(pendiente, pagado, delActivo));
        when(pagoRepository.findByComercioIdOrderByFechaCreacionDesc(1L)).thenReturn(List.of());

        service.listarComercio(usuarioComercio, null);

        verify(registrador).anularSiPendiente(1L);
        verify(registrador, never()).anularSiPendiente(2L);
        verify(registrador, never()).anularSiPendiente(3L);
        verify(registrador, never()).asegurarPendiente(any());
    }

    // ───────────── obtenerParaComercio ─────────────

    @Test
    @DisplayName("Obtener pago propio devuelve el DTO")
    void obtenerParaComercio_propio_ok() {
        Pedido pedido = pedido(100L, EstadoPedido.CREADO, "500", null);
        when(pagoRepository.findById(5L)).thenReturn(Optional.of(pago(5L, 100L, "500", EstadoPago.PENDIENTE)));
        stubLookupsDto(pedido);

        assertEquals(5L, service.obtenerParaComercio(5L, usuarioComercio).getId());
    }

    @Test
    @DisplayName("Obtener pago inexistente -> NoSuchElement; de otro comercio -> AccessDenied (BOLA)")
    void obtenerParaComercio_inexistenteOAjeno() {
        when(pagoRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.obtenerParaComercio(404L, usuarioComercio));

        Pago ajeno = pago(6L, 100L, "500", EstadoPago.PENDIENTE);
        ajeno.setComercioId(99L);
        when(pagoRepository.findById(6L)).thenReturn(Optional.of(ajeno));
        assertThrows(AccessDeniedException.class, () -> service.obtenerParaComercio(6L, usuarioComercio));
    }

    // ───────────── pagar ─────────────

    @Test
    @DisplayName("Pagar con tarjeta valida: confirma, guarda solo ultimos 4 digitos y registra al pagador")
    void pagar_tarjetaValida_confirmaPago() {
        Pago pago = pago(5L, 100L, "1500.00", EstadoPago.PENDIENTE);
        prepararPagoPendiente(pago, EstadoPedido.CREADO);
        when(pasarela.procesar(eq(MetodoPago.TARJETA_CREDITO), eq(TARJETA_OK), any(), any()))
                .thenReturn(PasarelaPago.ResultadoPasarela.aprobado("SIM-123"));

        PagoResponseDTO res = service.pagar(5L, dtoTarjeta("1500.00"), usuarioComercio);

        assertEquals(EstadoPago.PAGADO, pago.getEstado());
        assertEquals("4242", pago.getTarjetaUltimos4());
        assertEquals("SIM-123", pago.getReferenciaOperacion());
        assertEquals(10L, pago.getPagadoPorUsuarioId());
        assertEquals(MetodoPago.TARJETA_CREDITO, pago.getMetodoPago());
        assertEquals(EstadoPago.PAGADO, res.getEstado());
        verify(pagoRepository).save(pago);
    }

    @Test
    @DisplayName("Pagar por transferencia valida no guarda tarjeta")
    void pagar_transferenciaValida_sinTarjeta() {
        Pago pago = pago(5L, 100L, "800", EstadoPago.PENDIENTE);
        prepararPagoPendiente(pago, EstadoPedido.CREADO);
        when(pasarela.procesar(eq(MetodoPago.TRANSFERENCIA), any(), eq(CBU_OK), any()))
                .thenReturn(PasarelaPago.ResultadoPasarela.aprobado("SIM-TR"));
        PagarPedidoDTO dto = new PagarPedidoDTO();
        dto.setMetodoPago(MetodoPago.TRANSFERENCIA);
        dto.setMonto(new BigDecimal("800.00"));
        dto.setCbu(CBU_OK);

        service.pagar(5L, dto, usuarioComercio);

        assertEquals(EstadoPago.PAGADO, pago.getEstado());
        assertNull(pago.getTarjetaUltimos4());
    }

    @Test
    @DisplayName("Pagar: la pasarela rechaza -> PagoRechazadoException y el pago sigue PENDIENTE")
    void pagar_pasarelaRechaza_pagoQuedaPendiente() {
        Pago pago = pago(5L, 100L, "1500.00", EstadoPago.PENDIENTE);
        prepararPagoPendiente(pago, EstadoPedido.CREADO);
        when(pasarela.procesar(any(), any(), any(), any()))
                .thenReturn(PasarelaPago.ResultadoPasarela.rechazado("Fondos insuficientes"));

        PagoRechazadoException ex = assertThrows(PagoRechazadoException.class,
                () -> service.pagar(5L, dtoTarjeta("1500.00"), usuarioComercio));

        assertTrue(ex.getMessage().contains("Fondos insuficientes"));
        assertEquals(EstadoPago.PENDIENTE, pago.getEstado());
        verify(pagoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pagar: DTO nulo o sin metodo de pago -> IllegalArgument")
    void pagar_sinMetodo_rechaza() {
        assertThrows(IllegalArgumentException.class, () -> service.pagar(5L, null, usuarioComercio));
        assertThrows(IllegalArgumentException.class, () -> service.pagar(5L, new PagarPedidoDTO(), usuarioComercio));
    }

    @Test
    @DisplayName("Pagar: pago inexistente, ajeno, ya pagado o anulado")
    void pagar_estadosInvalidos() {
        when(pagoRepository.findByIdForUpdate(404L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.pagar(404L, dtoTarjeta("10"), usuarioComercio));

        Pago ajeno = pago(6L, 100L, "10", EstadoPago.PENDIENTE);
        ajeno.setComercioId(99L);
        when(pagoRepository.findByIdForUpdate(6L)).thenReturn(Optional.of(ajeno));
        assertThrows(AccessDeniedException.class, () -> service.pagar(6L, dtoTarjeta("10"), usuarioComercio));

        Pago pagado = pago(7L, 100L, "10", EstadoPago.PAGADO);
        when(pagoRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(pagado));
        assertThrows(IllegalStateException.class, () -> service.pagar(7L, dtoTarjeta("10"), usuarioComercio));

        Pago anulado = pago(8L, 100L, "10", EstadoPago.ANULADO);
        when(pagoRepository.findByIdForUpdate(8L)).thenReturn(Optional.of(anulado));
        assertThrows(IllegalStateException.class, () -> service.pagar(8L, dtoTarjeta("10"), usuarioComercio));
        verify(pasarela, never()).procesar(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Pagar: el pedido no existe o esta cancelado")
    void pagar_pedidoInexistenteOCancelado() {
        Pago pago = pago(5L, 100L, "10", EstadoPago.PENDIENTE);
        when(pagoRepository.findByIdForUpdate(5L)).thenReturn(Optional.of(pago));
        when(pedidosRepository.findById(100L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> service.pagar(5L, dtoTarjeta("10"), usuarioComercio));

        when(pedidosRepository.findById(100L))
                .thenReturn(Optional.of(pedido(100L, EstadoPedido.CANCELADO, "10", null)));
        assertThrows(IllegalStateException.class, () -> service.pagar(5L, dtoTarjeta("10"), usuarioComercio));
    }

    @Test
    @DisplayName("Pagar: monto distinto o nulo -> IllegalArgument (no se confia en el cliente)")
    void pagar_montoNoCoincide() {
        Pago pago = pago(5L, 100L, "1500.00", EstadoPago.PENDIENTE);
        prepararPagoPendiente(pago, EstadoPedido.CREADO);

        assertThrows(IllegalArgumentException.class,
                () -> service.pagar(5L, dtoTarjeta("1499.99"), usuarioComercio));
        PagarPedidoDTO sinMonto = dtoTarjeta("1");
        sinMonto.setMonto(null);
        assertThrows(IllegalArgumentException.class, () -> service.pagar(5L, sinMonto, usuarioComercio));
        verify(pasarela, never()).procesar(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Pagar: datos de medio de pago invalidos (tarjeta y CBU) se rechazan antes de la pasarela")
    void pagar_medioDePagoInvalido() {
        Pago pago = pago(5L, 100L, "1500.00", EstadoPago.PENDIENTE);
        prepararPagoPendiente(pago, EstadoPedido.CREADO);

        PagarPedidoDTO tarjetaMala = dtoTarjeta("1500.00");
        tarjetaMala.setNumeroTarjeta("1234567890123456");
        assertThrows(IllegalArgumentException.class, () -> service.pagar(5L, tarjetaMala, usuarioComercio));

        PagarPedidoDTO cbuMalo = new PagarPedidoDTO();
        cbuMalo.setMetodoPago(MetodoPago.TRANSFERENCIA);
        cbuMalo.setMonto(new BigDecimal("1500.00"));
        cbuMalo.setCbu("123");
        assertThrows(IllegalArgumentException.class, () -> service.pagar(5L, cbuMalo, usuarioComercio));
        verify(pasarela, never()).procesar(any(), any(), any(), any());
    }

    // ───────────── Repartidor ─────────────

    @Test
    @DisplayName("Repartidor: usuario sin rol/habilitacion o sin perfil -> AccessDenied")
    void repartidor_noAutorizado() {
        assertThrows(AccessDeniedException.class, () -> service.listarParaRepartidor(null, null));
        assertThrows(AccessDeniedException.class,
                () -> service.listarParaRepartidor(usuario(20L, Rol.COMERCIO, EstadoUsuario.VALIDADO), null));
        Usuario rep = usuario(20L, Rol.REPARTIDOR, EstadoUsuario.VALIDADO);
        when(repartidorRepository.findByUsuarioId(20L)).thenReturn(Optional.empty());
        assertThrows(AccessDeniedException.class, () -> service.listarParaRepartidor(rep, null));
    }

    @Test
    @DisplayName("Repartidor sin pedidos recibe lista vacia")
    void repartidor_sinPedidos_vacio() {
        Usuario rep = usuario(20L, Rol.REPARTIDOR, EstadoUsuario.VALIDADO);
        Repartidor repartidor = Repartidor.builder().id(2L).usuarioId(20L).estado(EstadoRepartidor.DISPONIBLE).build();
        when(repartidorRepository.findByUsuarioId(20L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(2L), anyList())).thenReturn(List.of());

        assertTrue(service.listarParaRepartidor(rep, null).isEmpty());
    }

    @Test
    @DisplayName("Repartidor sin filtro ve PENDIENTE y PAGADO (no ANULADO), con su nombre en el DTO")
    void repartidor_sinFiltro_excluyeAnulados() {
        Usuario rep = usuario(20L, Rol.REPARTIDOR, EstadoUsuario.VALIDADO);
        rep.setNombre("Carlos");
        rep.setApellido("Gomez");
        Repartidor repartidor = Repartidor.builder().id(2L).usuarioId(20L).estado(EstadoRepartidor.DISPONIBLE).build();
        Pedido p1 = pedido(100L, EstadoPedido.ENTREGADO, "500", repartidor);
        Pedido p2 = pedido(101L, EstadoPedido.EN_CAMINO, "500", repartidor);
        Pedido p3 = pedido(102L, EstadoPedido.ENTREGADO, "500", repartidor);
        when(repartidorRepository.findByUsuarioId(20L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(2L), anyList())).thenReturn(List.of(p1, p2, p3));
        Pago pagado = pago(1L, 100L, "500", EstadoPago.PAGADO);
        pagado.setFechaCreacion(LocalDateTime.now().minusDays(2));
        Pago pendiente = pago(2L, 101L, "500", EstadoPago.PENDIENTE);
        pendiente.setFechaCreacion(LocalDateTime.now().minusDays(1));
        Pago anulado = pago(3L, 102L, "500", EstadoPago.ANULADO);
        when(pagoRepository.findByPedidoIdIn(anyCollection())).thenReturn(List.of(pagado, pendiente, anulado));
        when(pedidosRepository.findAllById(anyCollection())).thenReturn(List.of(p1, p2, p3));
        when(comercioRepository.findAllById(anyCollection())).thenReturn(List.of(comercio));
        when(usuarioRepository.findAllById(anyCollection())).thenReturn(List.of(rep));

        List<PagoResponseDTO> res = service.listarParaRepartidor(rep, null);

        assertEquals(2, res.size());
        assertEquals(2L, res.get(0).getId(), "ordenados por fecha de creacion descendente");
        assertEquals("Carlos Gomez", res.get(0).getRepartidorNombre());
        assertEquals(2L, res.get(0).getRepartidorId());
    }

    @Test
    @DisplayName("Repartidor filtrando por estado solo ve ese estado")
    void repartidor_conFiltro_soloEseEstado() {
        Usuario rep = usuario(20L, Rol.REPARTIDOR, EstadoUsuario.VALIDADO);
        Repartidor repartidor = Repartidor.builder().id(2L).usuarioId(20L).estado(EstadoRepartidor.DISPONIBLE).build();
        Pedido p1 = pedido(100L, EstadoPedido.ENTREGADO, "500", repartidor);
        when(repartidorRepository.findByUsuarioId(20L)).thenReturn(Optional.of(repartidor));
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(2L), anyList())).thenReturn(List.of(p1));
        when(pagoRepository.findByPedidoIdIn(anyCollection()))
                .thenReturn(List.of(pago(1L, 100L, "500", EstadoPago.PAGADO)));

        assertTrue(service.listarParaRepartidor(rep, EstadoPago.PENDIENTE).isEmpty());
    }

    // ───────────── helpers ─────────────

    private void prepararPagoPendiente(Pago pago, EstadoPedido estadoPedido) {
        when(pagoRepository.findByIdForUpdate(pago.getId())).thenReturn(Optional.of(pago));
        Pedido pedido = pedido(pago.getPedidoId(), estadoPedido, pago.getMonto().toPlainString(), null);
        when(pedidosRepository.findById(pago.getPedidoId())).thenReturn(Optional.of(pedido));
        stubLookupsDto(pedido);
    }

    private void stubLookupsDto(Pedido pedido) {
        when(pedidosRepository.findAllById(anyCollection())).thenReturn(List.of(pedido));
        when(comercioRepository.findAllById(anyCollection())).thenReturn(List.of(comercio));
        when(usuarioRepository.findAllById(anyCollection())).thenReturn(List.of());
    }

    private PagarPedidoDTO dtoTarjeta(String monto) {
        PagarPedidoDTO dto = new PagarPedidoDTO();
        dto.setMetodoPago(MetodoPago.TARJETA_CREDITO);
        dto.setMonto(new BigDecimal(monto));
        dto.setNumeroTarjeta(TARJETA_OK);
        dto.setTitular("Juan Perez");
        dto.setVencimiento(YearMonth.now().plusYears(2).format(DateTimeFormatter.ofPattern("MM/yy")));
        dto.setCvv("123");
        return dto;
    }

    private Usuario usuario(Long id, Rol rol, EstadoUsuario estado) {
        return Usuario.builder().id(id).rol(rol).estado(estado).nombre("N").apellido("A").build();
    }

    private Pedido pedido(Long id, EstadoPedido estado, String costo, Repartidor repartidor) {
        return Pedido.builder().id(id).comercioId(1L).estado(estado)
                .costoEnvio(costo == null ? null : new BigDecimal(costo))
                .repartidor(repartidor).direccionOrigen("Origen 1").direccionDestino("Destino 2").build();
    }

    private Pago pago(Long id, Long pedidoId, String monto, EstadoPago estado) {
        return Pago.builder().id(id).pedidoId(pedidoId).comercioId(1L).monto(new BigDecimal(monto))
                .estado(estado).fechaCreacion(LocalDateTime.now()).build();
    }
}
