package com.example.DA2Back.ruteo.negocio;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.mapas.Coordenada;
import com.example.DA2Back.mapas.RutaCalculada;
import com.example.DA2Back.mapas.ServicioMapas;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.negocio.AccesoPedidos;
import com.example.DA2Back.ruteo.dto.ParadaRutaDTO;
import com.example.DA2Back.ruteo.dto.RutaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServicioDeRutasTest {

    private static final Coordenada ORIGEN = new Coordenada(-34.60, -58.40);
    private static final Coordenada DESTINO = new Coordenada(-34.62, -58.45);

    @Mock private PedidosRepository pedidosRepository;
    @Mock private DepositoRepository depositoRepository;
    @Mock private ServicioMapas mapas;
    @Mock private ComercioRepository comercioRepository;
    @Mock private DepositoRepository depositoParaAcceso;

    private ServicioDeRutas servicio;

    @BeforeEach
    void setUp() {
        AccesoPedidos acceso = new AccesoPedidos(comercioRepository, depositoParaAcceso);
        servicio = new ServicioDeRutas(pedidosRepository, depositoRepository, mapas, acceso);
        when(mapas.nombreProveedor()).thenReturn("LOCAL");
    }

    // ───────────── rutaDeRepartidor ─────────────

    @Test
    @DisplayName("Repartidor sin pedidos activos: ruta vacia con el proveedor de mapas")
    void repartidorSinPedidos_rutaVacia() {
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of());

        RutaDTO ruta = servicio.rutaDeRepartidor(1L, null, null);

        assertTrue(ruta.paradas().isEmpty());
        assertNull(ruta.puntoPartida());
        assertEquals(0, ruta.duracionTotalMin());
        assertEquals("LOCAL", ruta.metodo());
        assertFalse(ruta.estimada());
    }

    @Test
    @DisplayName("Ubicacion actual valida se informa como punto de partida; invalida o incompleta se ignora")
    void ubicacionActual_validaEInvalida() {
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of());

        assertNotNull(servicio.rutaDeRepartidor(1L, -34.6, -58.4).puntoPartida());
        assertNull(servicio.rutaDeRepartidor(1L, 999.0, -58.4).puntoPartida());
        assertNull(servicio.rutaDeRepartidor(1L, -34.6, null).puntoPartida());
        assertNull(servicio.rutaDeRepartidor(1L, null, -58.4).puntoPartida());
    }

    @Test
    @DisplayName("Pedido ASIGNADO sin partida: retiro primero, luego entrega; la 1.a parada no tiene tramo previo")
    void pedidoAsignado_retiroAntesQueEntrega() {
        Pedido p = pedido(10L, EstadoPedido.ASIGNADO, ORIGEN, DESTINO);
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of(p));
        when(mapas.calcularRuta(anyList())).thenReturn(new RutaCalculada(5.0, 20.0,
                List.of(new RutaCalculada.Tramo(5.0, 20.0)), List.of(ORIGEN, DESTINO), "OSRM", false));

        RutaDTO ruta = servicio.rutaDeRepartidor(1L, null, null);

        assertEquals(2, ruta.paradas().size());
        ParadaRutaDTO primera = ruta.paradas().get(0);
        ParadaRutaDTO segunda = ruta.paradas().get(1);
        assertEquals("RETIRO", primera.tipo());
        assertEquals("ENTREGA", segunda.tipo());
        assertEquals(1, primera.orden());
        assertEquals(0.0, primera.distanciaDesdeAnteriorKm());
        assertEquals(5.0, segunda.distanciaDesdeAnteriorKm());
        assertEquals(20, segunda.duracionAcumuladaMin());
        assertEquals(5.0, ruta.distanciaTotalKm());
        assertEquals(20, ruta.duracionTotalMin());
        assertEquals("OSRM", ruta.metodo());
        assertFalse(ruta.estimada());
        assertEquals(2, ruta.geometria().size());
    }

    @Test
    @DisplayName("Con ubicacion actual cada parada tiene su tramo (partida -> 1.a parada incluida)")
    void conPartida_cadaParadaTieneTramo() {
        Pedido p = pedido(10L, EstadoPedido.ASIGNADO, ORIGEN, DESTINO);
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of(p));
        when(mapas.calcularRuta(anyList())).thenReturn(new RutaCalculada(8.0, 30.0,
                List.of(new RutaCalculada.Tramo(3.0, 10.0), new RutaCalculada.Tramo(5.0, 20.0)),
                List.of(), "ESTIMACION_LINEAL", true));

        RutaDTO ruta = servicio.rutaDeRepartidor(1L, -34.59, -58.38);

        assertNotNull(ruta.puntoPartida());
        assertEquals(3.0, ruta.paradas().get(0).distanciaDesdeAnteriorKm());
        assertEquals(10, ruta.paradas().get(0).duracionDesdeAnteriorMin());
        assertEquals(30, ruta.paradas().get(1).duracionAcumuladaMin());
        assertTrue(ruta.estimada());
        assertEquals(3, ruta.geometria().size(), "sin geometria del proveedor se dibuja el recorrido directo");
    }

    @Test
    @DisplayName("Pedido EN_CAMINO solo tiene entrega; con un unico punto no se calcula ruta")
    void pedidoEnCamino_soloEntrega() {
        Pedido p = pedido(11L, EstadoPedido.EN_CAMINO, ORIGEN, DESTINO);
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of(p));

        RutaDTO ruta = servicio.rutaDeRepartidor(1L, null, null);

        assertEquals(1, ruta.paradas().size());
        assertEquals("ENTREGA", ruta.paradas().get(0).tipo());
        assertEquals(0.0, ruta.distanciaTotalKm());
        assertEquals("LOCAL", ruta.metodo());
        verify(mapas, never()).calcularRuta(anyList());
    }

    @Test
    @DisplayName("Pedidos sin coordenadas se geocodifican y se guardan")
    void pedidoSinCoordenadas_seGeocodificaYGuarda() {
        Pedido p = pedido(12L, EstadoPedido.EN_CAMINO, null, null);
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of(p));
        when(mapas.geocodificar(anyString())).thenReturn(Optional.of(DESTINO));

        RutaDTO ruta = servicio.rutaDeRepartidor(1L, null, null);

        assertEquals(1, ruta.paradas().size());
        assertEquals(DESTINO.lat(), p.getDestinoLat());
        verify(pedidosRepository).save(p);
    }

    @Test
    @DisplayName("Direcciones que no se pueden ubicar generan advertencias y no paradas")
    void direccionesNoUbicables_generanAdvertencias() {
        Pedido p = pedido(13L, EstadoPedido.ASIGNADO, null, null);
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of(p));
        when(mapas.geocodificar(any())).thenReturn(Optional.empty());

        RutaDTO ruta = servicio.rutaDeRepartidor(1L, null, null);

        assertTrue(ruta.paradas().isEmpty());
        assertEquals(2, ruta.advertencias().size());
        verify(pedidosRepository, never()).save(any());
    }

    @Test
    @DisplayName("Sin direccion de origen en el pedido se usa la del deposito")
    void sinDireccionOrigen_usaDeposito() {
        Pedido p = pedido(14L, EstadoPedido.ASIGNADO, null, DESTINO);
        p.setDireccionOrigen("  ");
        p.setDepositoId(5L);
        Deposito deposito = new Deposito();
        deposito.setDireccion("Deposito Central 123");
        when(depositoRepository.findById(5L)).thenReturn(Optional.of(deposito));
        when(pedidosRepository.findByRepartidorIdAndEstadoIn(eq(1L), anyList())).thenReturn(List.of(p));
        when(mapas.geocodificar("Deposito Central 123")).thenReturn(Optional.of(ORIGEN));
        when(mapas.calcularRuta(anyList())).thenReturn(new RutaCalculada(1, 1,
                List.of(new RutaCalculada.Tramo(1, 1)), List.of(), "X", false));

        RutaDTO ruta = servicio.rutaDeRepartidor(1L, null, null);

        assertEquals("RETIRO", ruta.paradas().get(0).tipo());
        assertEquals("Deposito Central 123", ruta.paradas().get(0).direccion());
    }

    // ───────────── rutaDePedido ─────────────

    @Test
    @DisplayName("Ruta de pedido para ADMIN: retiro -> entrega")
    void rutaDePedido_admin_ok() {
        Pedido p = pedido(20L, EstadoPedido.CREADO, ORIGEN, DESTINO);
        when(pedidosRepository.findById(20L)).thenReturn(Optional.of(p));
        when(mapas.calcularRuta(anyList())).thenReturn(new RutaCalculada(4, 15,
                List.of(new RutaCalculada.Tramo(4, 15)), List.of(), "OSRM", false));

        RutaDTO ruta = servicio.rutaDePedido(20L, usuario(1L, Rol.ADMIN));

        assertEquals(2, ruta.paradas().size());
        assertNull(ruta.puntoPartida());
    }

    @Test
    @DisplayName("Ruta de pedido inexistente -> NoSuchElement")
    void rutaDePedido_inexistente() {
        when(pedidosRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> servicio.rutaDePedido(99L, usuario(1L, Rol.ADMIN)));
    }

    @Test
    @DisplayName("Ruta de pedido de otro comercio -> AccessDenied; rol REPARTIDOR no permitido")
    void rutaDePedido_ajenoOSinPermiso() {
        Pedido p = pedido(21L, EstadoPedido.CREADO, ORIGEN, DESTINO); // comercio 1
        when(pedidosRepository.findById(21L)).thenReturn(Optional.of(p));
        Usuario comercioAjeno = usuario(7L, Rol.COMERCIO);
        when(comercioRepository.findByUsuarioId(7L))
                .thenReturn(Optional.of(Comercio.builder().id(99L).usuarioId(7L).build()));

        assertThrows(AccessDeniedException.class, () -> servicio.rutaDePedido(21L, comercioAjeno));
        assertThrows(AccessDeniedException.class, () -> servicio.rutaDePedido(21L, usuario(8L, Rol.REPARTIDOR)));
    }

    // ───────────── helpers ─────────────


    private Usuario usuario(Long id, Rol rol) {
        return Usuario.builder().id(id).rol(rol).estado(EstadoUsuario.VALIDADO).build();
    }

    private Pedido pedido(Long id, EstadoPedido estado, Coordenada origen, Coordenada destino) {
        Pedido p = Pedido.builder().id(id).comercioId(1L).depositoId(1L).estado(estado)
                .direccionOrigen("Origen 1").direccionDestino("Destino 2").build();
        if (origen != null) {
            p.setOrigenLat(origen.lat());
            p.setOrigenLng(origen.lng());
        }
        if (destino != null) {
            p.setDestinoLat(destino.lat());
            p.setDestinoLng(destino.lng());
        }
        return p;
    }
}
