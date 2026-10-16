package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedidoRepository;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.inventario.negocio.IInventario;
import com.example.DA2Back.pedidos.dato.DetallePedido;
import com.example.DA2Back.pedidos.dto.DetallePedidoResponseDTO;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.pedidos.dto.DetallePedidoDTO;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.HistorialEstadoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.pedidos.negocio.state.ResolverEstadoPedido;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para el ciclo de vida de los Pedidos.
 *
 * Es la ÚNICA puerta de entrada para mutar un Pedido. Utiliza el
 * patrón State (ResolverEstadoPedido) para que cada estado del pedido
 * defina qué transiciones son válidas, eliminando if/switch en este
 * servicio y garantizando consistencia con el módulo de Repartidores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioDePedidosImpl implements ServicioDePedidos {

    private static final List<EstadoPedido> ESTADOS_ACTIVOS =
            List.of(EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO);

    private final PedidosRepository pedidosRepository;
    private final RepartidorRepository repartidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComercioRepository comercioRepository;
    private final DepositoRepository depositoRepository;
    private final HistorialEstadoPedidoRepository historialRepository;
    private final ResolverEstadoPedido resolverEstado;
    private final IInventario inventarioService;
    private final ItemInventarioRepository itemInventarioRepository;
    private final ProductoRepository productoRepository;

    // -------------------------------------------------------------------------
    // Creación
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(CrearPedidoDTO dto) {
        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("El pedido debe incluir al menos un producto");
        }

        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.COMERCIO) {
            Comercio com = comercioRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new AccessDeniedException("No se encontró comercio asociado al usuario"));
            if (!Objects.equals(dto.getComercioId(), com.getId())) {
                throw new AccessDeniedException("No podés crear pedidos para otro comercio");
            }
        }

        Pedido pedido = Pedido.builder()
                .comercioId(dto.getComercioId())
                .depositoId(dto.getDepositoId())
                .direccionDestino(dto.getDireccionDestino())
                .direccionOrigen(dto.getDireccionOrigen())
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.CREADO)
                .build();

        Inventario inv = inventarioService.obtenerPorComercio(dto.getComercioId());
        for (DetallePedidoDTO d : dto.getDetalles()) {
            if (d.getCantidad() == null || d.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad del producto debe ser mayor a 0");
            }

            Producto prod = productoRepository.findById(d.getProductoId())
                    .orElseThrow(() -> new NoSuchElementException("Producto no encontrado: " + d.getProductoId()));

            ItemInventario item = itemInventarioRepository
                    .findByInventarioIdAndProductoIdAndDepositoId(inv.getId(), prod.getId(), dto.getDepositoId())
                    .orElseThrow(() -> new IllegalStateException("El producto " + prod.getNombre() + " no esta en el deposito seleccionado"));

            if (item.getCantidad() < d.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para el producto: " + prod.getNombre());
            }

            item.setCantidad(item.getCantidad() - d.getCantidad());
            itemInventarioRepository.save(item);

            DetallePedido detalle = DetallePedido.builder()
                    .pedido(pedido)
                    .producto(prod)
                    .cantidad(d.getCantidad())
                    .build();
            pedido.getDetalles().add(detalle);
        }

        Pedido guardado = pedidosRepository.save(pedido);
        registrarHistorial(guardado, EstadoPedido.CREADO);
        return toResponseDTO(guardado);
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id) {
        Pedido pedido = buscarPedido(id);
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null) {
            if (usuario.getRol() == Rol.COMERCIO) {
                Comercio com = comercioRepository.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("No se encontró comercio asociado al usuario"));
                if (!Objects.equals(pedido.getComercioId(), com.getId())) {
                    throw new AccessDeniedException("No podés consultar este pedido");
                }
            } else if (usuario.getRol() == Rol.DEPOSITO) {
                Deposito dep = depositoRepository.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("No se encontró depósito asociado al usuario"));
                if (!Objects.equals(pedido.getDepositoId(), dep.getId())) {
                    throw new AccessDeniedException("No podés consultar pedidos de otro depósito");
                }
            }
        }
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null) {
            if (usuario.getRol() == Rol.COMERCIO) {
                Comercio com = comercioRepository.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("No se encontró comercio asociado al usuario"));
                return pedidosRepository.findByComercioId(com.getId()).stream()
                        .map(this::toResponseDTO)
                        .collect(Collectors.toList());
            } else if (usuario.getRol() == Rol.DEPOSITO) {
                Deposito dep = depositoRepository.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("No se encontró depósito asociado al usuario"));
                return pedidosRepository.findByDepositoId(dep.getId()).stream()
                        .map(this::toResponseDTO)
                        .collect(Collectors.toList());
            }
        }
        return pedidosRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorComercio(Long comercioId) {
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.COMERCIO) {
            Comercio com = comercioRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new AccessDeniedException("No se encontró comercio asociado al usuario"));
            if (!Objects.equals(comercioId, com.getId())) {
                throw new AccessDeniedException("No podés consultar pedidos de otro comercio");
            }
        }
        return pedidosRepository.findByComercioId(comercioId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado) {
        return pedidosRepository.findByEstado(estado).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPendientesAsignables() {
        return pedidosRepository.findByEstadoAndRepartidorIsNull(EstadoPedido.LISTO_PARA_RETIRAR).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Transiciones de estado (via patrón State)
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public PedidoResponseDTO marcarListoParaRetirar(Long id) {
        Pedido pedido = buscarPedido(id);
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null) {
            if (usuario.getRol() == Rol.COMERCIO) {
                Comercio com = comercioRepository.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("No se encontró comercio asociado al usuario"));
                if (!Objects.equals(pedido.getComercioId(), com.getId())) {
                    throw new AccessDeniedException("No podés modificar este pedido");
                }
            } else if (usuario.getRol() == Rol.DEPOSITO) {
                Deposito dep = depositoRepository.findByUsuarioId(usuario.getId())
                        .orElseThrow(() -> new AccessDeniedException("No se encontró depósito asociado al usuario"));
                if (!Objects.equals(pedido.getDepositoId(), dep.getId())) {
                    throw new AccessDeniedException("No podés marcar listo para retirar un pedido de otro depósito");
                }
            }
        }
        resolverEstado.resolver(pedido.getEstado()).marcarListoParaRetirar(pedido);
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.LISTO_PARA_RETIRAR);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO asignarRepartidor(Long pedidoId, Long repartidorId) {
        Pedido pedido = buscarPedido(pedidoId);
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Repartidor con id=" + repartidorId + " no encontrado"));

        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.REPARTIDOR) {
            if (!Objects.equals(repartidor.getUsuarioId(), usuario.getId())) {
                throw new AccessDeniedException("No podés asignar un pedido a otro repartidor");
            }
        }

        // El estado actual del pedido valida si la transición es legal
        resolverEstado.resolver(pedido.getEstado()).asignarRepartidor(pedido, repartidor);

        pedidosRepository.save(pedido);
        repartidorRepository.save(repartidor);
        registrarHistorial(pedido, EstadoPedido.ASIGNADO);
        return toResponseDTO(pedido);
    }

    /**
     * El repartidor inicia el viaje: ASIGNADO → EN_CAMINO.
     */
    @Override
    @Transactional
    public PedidoResponseDTO marcarRetirado(Long id) {
        Pedido pedido = buscarPedido(id);
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.REPARTIDOR) {
            if (pedido.getRepartidor() != null && !Objects.equals(pedido.getRepartidor().getUsuarioId(), usuario.getId())) {
                throw new AccessDeniedException("No podés operar sobre un pedido asignado a otro repartidor");
            }
        }
        resolverEstado.resolver(pedido.getEstado()).marcarRetirado(pedido);
        if (pedido.getRepartidor() != null) {
            pedido.getRepartidor().setEstado(EstadoRepartidor.EN_ENTREGA);
            repartidorRepository.save(pedido.getRepartidor());
        }
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.RETIRADO);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO iniciarViaje(Long id) {
        Pedido pedido = buscarPedido(id);
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.REPARTIDOR) {
            if (pedido.getRepartidor() != null && !Objects.equals(pedido.getRepartidor().getUsuarioId(), usuario.getId())) {
                throw new AccessDeniedException("No podés operar sobre un pedido asignado a otro repartidor");
            }
        }
        resolverEstado.resolver(pedido.getEstado()).iniciarViaje(pedido);
        if (pedido.getRepartidor() != null) {
            pedido.getRepartidor().setEstado(EstadoRepartidor.EN_ENTREGA);
            repartidorRepository.save(pedido.getRepartidor());
        }
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.EN_CAMINO);
        return toResponseDTO(pedido);
    }

    /**
     * El repartidor confirma la entrega: EN_CAMINO → ENTREGADO.
     * Libera al repartidor si no tiene otros pedidos activos.
     */
    @Override
    @Transactional
    public PedidoResponseDTO entregar(Long id) {
        Pedido pedido = buscarPedido(id);
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.REPARTIDOR) {
            if (pedido.getRepartidor() != null && !Objects.equals(pedido.getRepartidor().getUsuarioId(), usuario.getId())) {
                throw new AccessDeniedException("No podés operar sobre un pedido asignado a otro repartidor");
            }
        }
        resolverEstado.resolver(pedido.getEstado()).entregar(pedido);
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.ENTREGADO);
        liberarRepartidorSiCorresponde(pedido);
        return toResponseDTO(pedido);
    }

    /**
     * Cancela el pedido desde cualquier estado que lo permita.
     * Restaura el stock en el inventario del depósito y libera al repartidor si estaba asignado.
     */
    @Override
    @Transactional
    public PedidoResponseDTO cancelar(Long id) {
        Pedido pedido = buscarPedido(id);
        Usuario usuario = getUsuarioAutenticado();
        if (usuario != null && usuario.getRol() == Rol.COMERCIO) {
            Comercio com = comercioRepository.findByUsuarioId(usuario.getId())
                    .orElseThrow(() -> new AccessDeniedException("No se encontró comercio asociado al usuario"));
            if (!Objects.equals(pedido.getComercioId(), com.getId())) {
                throw new AccessDeniedException("No podés cancelar este pedido");
            }
        }
        resolverEstado.resolver(pedido.getEstado()).cancelar(pedido);
        restaurarStock(pedido);
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.CANCELADO);
        liberarRepartidorSiCorresponde(pedido);
        return toResponseDTO(pedido);
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private Pedido buscarPedido(Long id) {
        return pedidosRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido con id=" + id + " no encontrado"));
    }

    private void registrarHistorial(Pedido pedido, EstadoPedido estado) {
        HistorialEstadoPedido historial = HistorialEstadoPedido.builder()
                .pedido(pedido)
                .estado(estado)
                .fechaHora(LocalDateTime.now())
                .build();
        HistorialEstadoPedido guardado = historialRepository.save(historial);
        if (pedido.getHistorial() != null) {
            pedido.getHistorial().add(guardado != null ? guardado : historial);
        }
    }

    /**
     * Si el pedido terminó (ENTREGADO o CANCELADO) y el repartidor
     * no tiene otros pedidos activos, lo devuelve al estado DISPONIBLE.
     */
    private void liberarRepartidorSiCorresponde(Pedido pedido) {
        if (pedido.getRepartidor() == null) return;

        Repartidor repartidor = pedido.getRepartidor();
        boolean tieneOtroActivo = pedidosRepository
                .existsByRepartidorIdAndEstadoIn(repartidor.getId(), ESTADOS_ACTIVOS);

        if (!tieneOtroActivo && repartidor.isActivo()) {
            repartidor.setEstado(EstadoRepartidor.DISPONIBLE);
            repartidorRepository.save(repartidor);
        }
    }

        private PedidoResponseDTO toResponseDTO(Pedido pedido) {
        Long repartidorId = pedido.getRepartidor() != null ? pedido.getRepartidor().getId() : null;
        String repartidorNombre = null;
        if (pedido.getRepartidor() != null && pedido.getRepartidor().getUsuarioId() != null) {
            Usuario usuario = buscarUsuario(pedido.getRepartidor().getUsuarioId());
            if (usuario != null) {
                repartidorNombre = usuario.getNombre() + " " + usuario.getApellido();
            }
        }

        List<HistorialEstadoDTO> historialDTO = pedido.getHistorial() != null
                ? pedido.getHistorial().stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(HistorialEstadoPedido::getFechaHora, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                    .map(h -> HistorialEstadoDTO.builder()
                        .id(h.getId())
                        .estado(h.getEstado())
                        .fechaHora(h.getFechaHora())
                        .build())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        List<DetallePedidoResponseDTO> detallesDTO = pedido.getDetalles() != null
                ? pedido.getDetalles().stream()
                    .map(d -> DetallePedidoResponseDTO.builder()
                        .id(d.getId())
                        .productoId(d.getProducto().getId())
                        .productoNombre(d.getProducto().getNombre())
                        .cantidad(d.getCantidad())
                        .build())
                    .collect(Collectors.toList())
                : new ArrayList<>();

        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .comercioId(pedido.getComercioId())
                .depositoId(pedido.getDepositoId())
                .direccionDestino(pedido.getDireccionDestino())
                .direccionOrigen(pedido.getDireccionOrigen())
                .fechaCreacion(pedido.getFechaCreacion())
                .estado(pedido.getEstado())
                .repartidorId(repartidorId)
                .repartidorNombre(repartidorNombre)
                .historial(historialDTO)
                .detalles(detallesDTO)
                .build();
    }

    private void restaurarStock(Pedido pedido) {
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty() || pedido.getDepositoId() == null) {
            return;
        }
        try {
            Inventario inv = inventarioService.obtenerPorComercio(pedido.getComercioId());
            if (inv == null) return;
            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.getProducto() != null && detalle.getCantidad() != null) {
                    itemInventarioRepository
                            .findByInventarioIdAndProductoIdAndDepositoId(
                                    inv.getId(), detalle.getProducto().getId(), pedido.getDepositoId())
                            .ifPresent(item -> {
                                item.setCantidad(item.getCantidad() + detalle.getCantidad());
                                itemInventarioRepository.save(item);
                            });
                }
            }
        } catch (Exception e) {
            log.error("Error al restaurar stock del pedido {}: {}", pedido.getId(), e.getMessage(), e);
        }
    }

    private Usuario buscarUsuario(Long usuarioId) {
        if (usuarioId == null) return null;
        return usuarioRepository.findById(usuarioId).orElse(null);
    }

    private Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof Usuario u) {
            return u;
        }
        if (principal instanceof UserDetails ud) {
            return usuarioRepository.findByEmail(ud.getUsername()).orElse(null);
        }
        if (principal instanceof String email) {
            return usuarioRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
}



