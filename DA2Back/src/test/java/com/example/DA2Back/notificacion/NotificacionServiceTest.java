package com.example.DA2Back.notificacion;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.notificacion.dato.CanalNotificacion;
import com.example.DA2Back.notificacion.dato.Notificacion;
import com.example.DA2Back.notificacion.dato.NotificacionRepository;
import com.example.DA2Back.notificacion.dato.TipoNotificacion;
import com.example.DA2Back.notificacion.dto.NotificacionResponseDTO;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.repartidor.dato.Repartidor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificacionServiceTest {

    @Mock private NotificacionRepository notificacionRepository;
    @Mock private ComercioRepository comercioRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificacionService service;

    // ───────────── actualizacion de pedido ─────────────

    @Test
    @DisplayName("Actualizacion de pedido genera una notificacion APP (no leida, enviada) y un EMAIL pendiente")
    void actualizacionPedido_generaAppYEmail() {
        Pedido pedido = Pedido.builder().id(50L).comercioId(1L).build();
        when(comercioRepository.findById(1L)).thenReturn(Optional.of(Comercio.builder().id(1L).usuarioId(10L).build()));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L, EstadoUsuario.VALIDADO)));

        service.notificarActualizacionPedido(pedido, EstadoPedido.ENTREGADO);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(2)).save(captor.capture());
        Notificacion app = captor.getAllValues().get(0);
        Notificacion email = captor.getAllValues().get(1);
        assertEquals(CanalNotificacion.APP, app.getCanal());
        assertFalse(app.isLeida());
        assertTrue(app.isEnviada());
        assertTrue(app.getMensaje().contains("#50"));
        assertTrue(app.getMensaje().contains("ENTREGADO"));
        assertEquals(CanalNotificacion.EMAIL, email.getCanal());
        assertFalse(email.isEnviada());
        assertTrue(email.isLeida());
        assertEquals("u10@mail.com", email.getDestinatarioEmail());
    }

    @Test
    @DisplayName("Actualizacion de pedido: comercio inexistente o sin usuario en base no notifica; comercio sin usuarioId falla")
    void actualizacionPedido_casosBorde() {
        Pedido pedido = Pedido.builder().id(50L).comercioId(1L).build();
        when(comercioRepository.findById(1L)).thenReturn(Optional.empty());
        service.notificarActualizacionPedido(pedido, EstadoPedido.CREADO);

        when(comercioRepository.findById(1L)).thenReturn(Optional.of(Comercio.builder().id(1L).usuarioId(10L).build()));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());
        service.notificarActualizacionPedido(pedido, EstadoPedido.CREADO);
        verify(notificacionRepository, never()).save(any());

        when(comercioRepository.findById(1L)).thenReturn(Optional.of(Comercio.builder().id(1L).usuarioId(null).build()));
        assertThrows(IllegalStateException.class, () -> service.notificarActualizacionPedido(pedido, EstadoPedido.CREADO));
    }

    // ───────────── pedido asignado ─────────────

    @Test
    @DisplayName("Pedido asignado: el mensaje incluye direcciones y usa 'No informado' cuando faltan")
    void pedidoAsignado_mensajeConDefectos() {
        Pedido pedido = Pedido.builder().id(60L).direccionOrigen("Deposito 1").direccionDestino(null)
                .fechaCreacion(null).build();
        Repartidor repartidor = Repartidor.builder().id(2L).usuarioId(20L).build();
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuario(20L, EstadoUsuario.VALIDADO)));

        service.notificarPedidoAsignado(pedido, repartidor);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository, times(2)).save(captor.capture());
        String mensaje = captor.getAllValues().get(0).getMensaje();
        assertTrue(mensaje.contains("Deposito 1"));
        assertTrue(mensaje.contains("No informado"));
        assertEquals(TipoNotificacion.PEDIDO_ASIGNADO, captor.getAllValues().get(0).getTipo());
    }

    @Test
    @DisplayName("Pedido asignado: si no existe el usuario del repartidor no se notifica")
    void pedidoAsignado_sinUsuario() {
        when(usuarioRepository.findById(20L)).thenReturn(Optional.empty());

        service.notificarPedidoAsignado(Pedido.builder().id(60L).build(), Repartidor.builder().id(2L).usuarioId(20L).build());

        verify(notificacionRepository, never()).save(any());
    }

    // ───────────── cuenta ─────────────

    @Test
    @DisplayName("Solicitud de registro genera un email pendiente")
    void solicitudRegistro() {
        service.notificarSolicitudRegistro(usuario(10L, EstadoUsuario.EN_EVALUACION));

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());
        assertEquals(TipoNotificacion.SOLICITUD_REGISTRO, captor.getValue().getTipo());
        assertEquals(CanalNotificacion.EMAIL, captor.getValue().getCanal());
    }

    @Test
    @DisplayName("Cambio de estado de cuenta: notifica VALIDADO, RECHAZADO y BLOQUEADO; EN_EVALUACION no")
    void cambioEstadoUsuario() {
        service.notificarActualizacionEstadoUsuario(usuario(10L, EstadoUsuario.VALIDADO));
        service.notificarActualizacionEstadoUsuario(usuario(10L, EstadoUsuario.RECHAZADO));
        service.notificarActualizacionEstadoUsuario(usuario(10L, EstadoUsuario.BLOQUEADO));
        verify(notificacionRepository, times(3)).save(any(Notificacion.class));

        service.notificarActualizacionEstadoUsuario(usuario(10L, EstadoUsuario.EN_EVALUACION));
        verify(notificacionRepository, times(3)).save(any(Notificacion.class));
    }

    // ───────────── stock ─────────────

    @Test
    @DisplayName("Stock bajo: item sin inventario o sin comercio -> IllegalState")
    void stockBajo_sinComercio() {
        ItemInventario sinInventario = new ItemInventario();
        assertThrows(IllegalStateException.class, () -> service.notificarStockBajo(sinInventario));

        ItemInventario sinComercio = new ItemInventario();
        sinComercio.setInventario(new Inventario());
        assertThrows(IllegalStateException.class, () -> service.notificarStockBajo(sinComercio));
    }

    @Test
    @DisplayName("Stock bajo notifica por APP al dueño del comercio")
    void stockBajo_ok() {
        ItemInventario item = item(3, 5);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L, EstadoUsuario.VALIDADO)));

        service.notificarStockBajo(item);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificacionRepository).save(captor.capture());
        assertEquals(TipoNotificacion.STOCK_BAJO, captor.getValue().getTipo());
        assertTrue(captor.getValue().getMensaje().contains("Zapatillas"));
    }

    @Test
    @DisplayName("Stock bajo sin usuario dueño en base no genera notificacion")
    void stockBajo_sinUsuario() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());
        service.notificarStockBajo(item(3, 5));
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Solo notifica al CRUZAR el minimo (de >= minimo a < minimo); usa 5 si no hay minimo")
    void cruceDeStockMinimo() {
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L, EstadoUsuario.VALIDADO)));

        service.notificarSiCruzoStockMinimo(item(4, 5), 5, 4);   // cruza -> notifica
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));

        service.notificarSiCruzoStockMinimo(item(3, 5), 4, 3);   // ya estaba bajo -> no
        service.notificarSiCruzoStockMinimo(item(9, 5), 8, 9);   // sube -> no
        service.notificarSiCruzoStockMinimo(item(5, 5), 6, 5);   // igual al minimo -> no
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));

        service.notificarSiCruzoStockMinimo(item(4, null), 5, 4); // minimo por defecto = 5 -> notifica
        verify(notificacionRepository, times(2)).save(any(Notificacion.class));
    }

    // ───────────── consulta del usuario ─────────────

    @Test
    @DisplayName("listarApp y contarNoLeidas: usuario invalido -> AccessDenied; valido -> DTOs / conteo")
    void listarYContar() {
        assertThrows(AccessDeniedException.class, () -> service.listarApp(null));
        assertThrows(AccessDeniedException.class, () -> service.contarNoLeidas(usuario(null, EstadoUsuario.VALIDADO)));

        Usuario u = usuario(10L, EstadoUsuario.VALIDADO);
        Notificacion n = notificacion(1L, u, CanalNotificacion.APP);
        when(notificacionRepository.findByUsuarioIdAndCanalOrderByFechaCreacionDesc(10L, CanalNotificacion.APP))
                .thenReturn(List.of(n));
        when(notificacionRepository.countByUsuarioIdAndCanalAndLeidaFalse(10L, CanalNotificacion.APP)).thenReturn(4L);

        List<NotificacionResponseDTO> lista = service.listarApp(u);

        assertEquals(1, lista.size());
        assertEquals("Titulo", lista.get(0).getTitulo());
        assertEquals(4L, service.contarNoLeidas(u));
    }

    @Test
    @DisplayName("marcarLeida: inexistente, de otro usuario (BOLA), de canal EMAIL y caso ok")
    void marcarLeida() {
        Usuario dueno = usuario(10L, EstadoUsuario.VALIDADO);
        assertThrows(AccessDeniedException.class, () -> service.marcarLeida(1L, null));

        when(notificacionRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.marcarLeida(404L, dueno));

        Notificacion ajena = notificacion(2L, usuario(99L, EstadoUsuario.VALIDADO), CanalNotificacion.APP);
        when(notificacionRepository.findById(2L)).thenReturn(Optional.of(ajena));
        assertThrows(AccessDeniedException.class, () -> service.marcarLeida(2L, dueno));

        Notificacion porEmail = notificacion(3L, dueno, CanalNotificacion.EMAIL);
        when(notificacionRepository.findById(3L)).thenReturn(Optional.of(porEmail));
        assertThrows(AccessDeniedException.class, () -> service.marcarLeida(3L, dueno));
        verify(notificacionRepository, never()).save(any());

        Notificacion propia = notificacion(4L, dueno, CanalNotificacion.APP);
        when(notificacionRepository.findById(4L)).thenReturn(Optional.of(propia));
        when(notificacionRepository.save(propia)).thenReturn(propia);
        NotificacionResponseDTO dto = service.marcarLeida(4L, dueno);
        assertTrue(propia.isLeida());
        assertTrue(dto.isLeida());
    }

    // ───────────── helpers ─────────────

    private Usuario usuario(Long id, EstadoUsuario estado) {
        return Usuario.builder().id(id).nombre("Nombre").apellido("Apellido")
                .email("u" + id + "@mail.com").estado(estado).build();
    }

    private ItemInventario item(int cantidad, Integer minimo) {
        Comercio comercio = Comercio.builder().id(1L).usuarioId(10L).build();
        Inventario inventario = new Inventario();
        inventario.setComercio(comercio);
        Producto producto = new Producto();
        producto.setNombre("Zapatillas");
        ItemInventario item = new ItemInventario();
        item.setId(8L);
        item.setInventario(inventario);
        item.setProducto(producto);
        item.setCantidad(cantidad);
        item.setStockMinimo(minimo);
        return item;
    }

    private Notificacion notificacion(Long id, Usuario usuario, CanalNotificacion canal) {
        Notificacion n = new Notificacion();
        n.setId(id);
        n.setUsuario(usuario);
        n.setCanal(canal);
        n.setTipo(TipoNotificacion.STOCK_BAJO);
        n.setTitulo("Titulo");
        n.setMensaje("Mensaje");
        n.setFechaCreacion(LocalDateTime.now());
        return n;
    }
}
