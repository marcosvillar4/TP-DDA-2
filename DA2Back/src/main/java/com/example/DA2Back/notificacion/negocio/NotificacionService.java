package com.example.DA2Back.notificacion.negocio;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.notificacion.dato.CanalNotificacion;
import com.example.DA2Back.notificacion.dato.Notificacion;
import com.example.DA2Back.notificacion.dato.NotificacionRepository;
import com.example.DA2Back.notificacion.dato.TipoNotificacion;
import com.example.DA2Back.notificacion.dto.NotificacionResponseDTO;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final NotificacionRepository notificacionRepository;
    private final ComercioRepository comercioRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public void notificarActualizacionPedido(Pedido pedido, EstadoPedido estado) {
        Comercio comercio = comercioRepository.findById(pedido.getComercioId()).orElse(null);
        if (comercio == null) {
            log.warn("No se pudo notificar: no existe el comercio {}", pedido.getComercioId());
            return;
        }

        Usuario usuario = buscarUsuario(comercio);
        if (usuario == null) {
            return;
        }
        String mensaje = "El pedido #" + pedido.getId() + " cambió a estado " + estado + ".";

        guardar(usuario, TipoNotificacion.ACTUALIZACION_PEDIDO, CanalNotificacion.APP,
                "Actualización de pedido", mensaje, pedido.getId(), true, null);
        guardar(usuario, TipoNotificacion.ACTUALIZACION_PEDIDO, CanalNotificacion.EMAIL,
                "Actualización del pedido #" + pedido.getId(), mensaje, pedido.getId(),
                false, usuario.getEmail());
    }

    @Transactional
    public void notificarPedidoAsignado(Pedido pedido, Repartidor repartidor) {
        Usuario usuario = usuarioRepository.findById(repartidor.getUsuarioId()).orElse(null);
        if (usuario == null) {
            log.warn("No se pudo notificar la asignación del pedido {}: "
                    + "no existe el usuario del repartidor {}",
                    pedido.getId(), repartidor.getId());
            return;
        }

        String mensaje = "Se te asignó el pedido #" + pedido.getId() + ".\n"
                + "Dirección de retiro: " + valorOrDefecto(pedido.getDireccionOrigen()) + ".\n"
                + "Dirección de entrega: " + valorOrDefecto(pedido.getDireccionDestino()) + ".\n"
                + "Fecha de creación: " + valorOrDefecto(pedido.getFechaCreacion()) + ".\n"
                + "Ingresá a tu perfil para retirar el pedido e iniciar la entrega.";

        guardar(usuario, TipoNotificacion.PEDIDO_ASIGNADO, CanalNotificacion.APP,
                "Nuevo pedido asignado", mensaje, pedido.getId(), true, null);
        guardar(usuario, TipoNotificacion.PEDIDO_ASIGNADO, CanalNotificacion.EMAIL,
                "Nuevo pedido asignado #" + pedido.getId(), mensaje, pedido.getId(),
                false, usuario.getEmail());
    }

    @Transactional
    public void notificarSolicitudRegistro(Usuario usuario) {
        String mensaje = "Hola " + usuario.getNombre() + ", recibimos tu solicitud de creación "
                + "de cuenta. Un administrador revisará tus datos y te avisaremos cuando tu "
                + "cuenta sea validada.";
        guardar(usuario, TipoNotificacion.SOLICITUD_REGISTRO, CanalNotificacion.EMAIL,
                "Solicitud de cuenta recibida", mensaje, usuario.getId(),
                false, usuario.getEmail());
    }

    @Transactional
    public void notificarActualizacionEstadoUsuario(Usuario usuario) {
        EstadoUsuario estado = usuario.getEstado();
        String estadoLegible = switch (estado) {
            case VALIDADO -> "validada";
            case RECHAZADO -> "rechazada";
            case BLOQUEADO -> "bloqueada";
            default -> null;
        };

        if (estadoLegible == null) {
            return;
        }

        String mensaje = "Tu cuenta fue " + estadoLegible
                + " por un administrador.";
        guardar(usuario, TipoNotificacion.ACTUALIZACION_ESTADO_USUARIO,
                CanalNotificacion.EMAIL, "Actualización del estado de tu cuenta",
                mensaje, usuario.getId(), false, usuario.getEmail());
    }

    @Transactional
    public void notificarStockBajo(ItemInventario item) {
        if (item.getInventario() == null || item.getInventario().getComercio() == null) {
            throw new IllegalStateException("El item de inventario no tiene comercio asociado");
        }
        Comercio comercio = item.getInventario().getComercio();
        Usuario usuario = buscarUsuario(comercio);
        if (usuario == null) {
            return;
        }
        String mensaje = "El producto " + item.getProducto().getNombre()
                + " tiene stock " + item.getCantidad()
                + ", por debajo del mínimo " + item.getStockMinimo() + ".";

        guardar(usuario, TipoNotificacion.STOCK_BAJO, CanalNotificacion.APP,
                "Stock bajo", mensaje, item.getId(), true, null);
    }

    public void notificarSiCruzoStockMinimo(ItemInventario item, int cantidadAnterior,
                                             int cantidadNueva) {
        int minimo = item.getStockMinimo() != null ? item.getStockMinimo() : 5;
        if (cantidadAnterior >= minimo && cantidadNueva < minimo) {
            notificarStockBajo(item);
        }
    }

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarApp(Usuario usuario) {
        validarUsuario(usuario);
        return notificacionRepository
                .findByUsuarioIdAndCanalOrderByFechaCreacionDesc(usuario.getId(), CanalNotificacion.APP)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Usuario usuario) {
        validarUsuario(usuario);
        return notificacionRepository.countByUsuarioIdAndCanalAndLeidaFalse(
                usuario.getId(), CanalNotificacion.APP);
    }

    @Transactional
    public NotificacionResponseDTO marcarLeida(Long id, Usuario usuario) {
        validarUsuario(usuario);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notificación no encontrada"));
        if (!notificacion.getUsuario().getId().equals(usuario.getId())
                || notificacion.getCanal() != CanalNotificacion.APP) {
            throw new AccessDeniedException("No tenés acceso a esta notificación");
        }
        notificacion.setLeida(true);
        return toDTO(notificacionRepository.save(notificacion));
    }

    private Usuario buscarUsuario(Comercio comercio) {
        if (comercio.getUsuarioId() == null) {
            throw new IllegalStateException("El comercio no tiene usuario asociado");
        }
        Usuario usuario = usuarioRepository.findById(comercio.getUsuarioId()).orElse(null);
        if (usuario == null) {
            log.warn("No se pudo notificar: no existe el usuario del comercio {}",
                    comercio.getId());
        }
        return usuario;
    }

    private void guardar(Usuario usuario, TipoNotificacion tipo, CanalNotificacion canal,
                         String titulo, String mensaje, Long referenciaId,
                         boolean enviada, String destinatarioEmail) {
        Notificacion notificacion = new Notificacion();
        notificacion.setUsuario(usuario);
        notificacion.setTipo(tipo);
        notificacion.setCanal(canal);
        notificacion.setTitulo(titulo);
        notificacion.setMensaje(mensaje);
        notificacion.setReferenciaId(referenciaId);
        notificacion.setLeida(canal != CanalNotificacion.APP);
        notificacion.setEnviada(enviada);
        notificacion.setDestinatarioEmail(destinatarioEmail);
        notificacion.setFechaCreacion(LocalDateTime.now());
        notificacionRepository.save(notificacion);
    }

    private void validarUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new AccessDeniedException("Usuario autenticado inválido");
        }
    }

    private NotificacionResponseDTO toDTO(Notificacion n) {
        return NotificacionResponseDTO.builder()
                .id(n.getId()).tipo(n.getTipo()).canal(n.getCanal())
                .titulo(n.getTitulo()).mensaje(n.getMensaje())
                .referenciaId(n.getReferenciaId()).leida(n.isLeida())
                .enviada(n.isEnviada()).fechaCreacion(n.getFechaCreacion()).build();
    }

    private String valorOrDefecto(Object valor) {
        return valor == null ? "No informado" : valor.toString();
    }
}
