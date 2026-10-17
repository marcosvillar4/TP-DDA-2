package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.EstadoHistoricoPedido;
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
import com.example.DA2Back.pedidos.negocio.evento.PedidoListoParaRetirarEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Servicio de dominio para el ciclo de vida de los Pedidos.
 *
 * Es la ÚNICA puerta de entrada para mutar un Pedido. Utiliza el
 * patrón State (ResolverEstadoPedido) para que cada estado del pedido
 * defina qué transiciones son válidas, eliminando if/switch en este
 * servicio y garantizando consistencia con el módulo de Repartidores.
 */
@Service
@RequiredArgsConstructor
public class ServicioDePedidosImpl implements ServicioDePedidos {

    private static final List<EstadoPedido> ESTADOS_ACTIVOS =
            List.of(EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO);

        private final PedidosRepository pedidosRepository;
    private final RepartidorRepository repartidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialEstadoPedidoRepository historialRepository;
    private final ResolverEstadoPedido resolverEstado;
    private final IInventario inventarioService;
    private final ItemInventarioRepository itemInventarioRepository;
    private final ProductoRepository productoRepository;
    private final DepositoRepository depositoRepository;
    private final AccesoPedidos accesoPedidos;
    private final ApplicationEventPublisher eventPublisher;

    // -------------------------------------------------------------------------
    // Creación
    // -------------------------------------------------------------------------

        @Override
    @Transactional
    public PedidoResponseDTO crearPedido(CrearPedidoDTO dto, Usuario usuario) {
        var alcance = accesoPedidos.resolver(usuario, Rol.ADMIN, Rol.COMERCIO);
        if (dto == null) throw new IllegalArgumentException("El pedido es obligatorio");
        Long comercioId = alcance.comercioId() != null ? alcance.comercioId() : dto.getComercioId();
        if (dto.getComercioId() != null) alcance.validarComercio(dto.getComercioId());
        validarDepositoDelComercio(comercioId, dto.getDepositoId());

        // Resolver y validar todos los items antes de descontar una sola unidad.
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        if (dto.getDetalles() != null) {
            for (DetallePedidoDTO d : dto.getDetalles()) {
                if (d == null || d.getProductoId() == null || d.getCantidad() == null || d.getCantidad() <= 0) {
                    throw new IllegalArgumentException("Cada detalle requiere producto y cantidad positiva");
                }
                long total = (long) cantidades.getOrDefault(d.getProductoId(), 0) + d.getCantidad();
                if (total > Integer.MAX_VALUE) throw new IllegalArgumentException("Cantidad fuera de rango");
                cantidades.put(d.getProductoId(), (int) total);
            }
        }
        Map<Long, ItemInventario> items = new LinkedHashMap<>();
        if (!cantidades.isEmpty()) {
            Inventario inv = inventarioService.obtenerPorComercio(comercioId);
            validarComercioRelacionado(comercioId, inv.getComercio() == null ? null : inv.getComercio().getId());
            for (var entrada : cantidades.entrySet()) {
                Producto prod = productoRepository.findById(entrada.getKey())
                        .orElseThrow(() -> new NoSuchElementException("Producto no encontrado"));
                validarComercioRelacionado(comercioId, prod.getComercio() == null ? null : prod.getComercio().getId());
                ItemInventario item = itemInventarioRepository
                        .findByInventarioIdAndProductoIdAndDepositoId(inv.getId(), prod.getId(), dto.getDepositoId())
                        .orElseThrow(() -> new IllegalStateException("Producto sin stock en el deposito seleccionado"));
                validarItemDelComercio(item, comercioId);
                if (item.getCantidad() == null || item.getCantidad() < entrada.getValue()) {
                    throw new IllegalStateException("Stock insuficiente para el producto: " + prod.getNombre());
                }
                items.put(prod.getId(), item);
            }
        }
        Pedido pedido = Pedido.builder()
                .comercioId(comercioId)
                .depositoId(dto.getDepositoId())
                .direccionDestino(dto.getDireccionDestino())
                .direccionOrigen(dto.getDireccionOrigen())
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.CREADO)
                .build();

        if (dto.getDetalles() != null && !dto.getDetalles().isEmpty()) {
            for (DetallePedidoDTO d : dto.getDetalles()) {
                ItemInventario item = items.get(d.getProductoId());
                Producto prod = item.getProducto();
                item.setCantidad(item.getCantidad() - d.getCantidad());
                itemInventarioRepository.save(item);

                DetallePedido detalle = DetallePedido.builder()
                        .pedido(pedido)
                        .producto(prod)
                        .cantidad(d.getCantidad())
                        .build();
                pedido.getDetalles().add(detalle);
            }
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
    public PedidoResponseDTO obtenerPorId(Long id, Usuario usuario) {
        var alcance = accesoPedidos.resolver(usuario, Rol.ADMIN, Rol.COMERCIO, Rol.DEPOSITO);
        Pedido pedido = buscarPedido(id);
        alcance.validarPedido(pedido);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos(Usuario usuario) {
        return consultarEnAlcance(usuario, null, null, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorComercio(Long comercioId, Usuario usuario) {
        return consultarEnAlcance(usuario, comercioId, null, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado, Usuario usuario) {
        return consultarEnAlcance(usuario, null, estado, false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPendientesAsignables(Usuario usuario) {
        return consultarEnAlcance(usuario, null, EstadoPedido.LISTO_PARA_RETIRAR, true);
    }

    private List<PedidoResponseDTO> consultarEnAlcance(Usuario usuario, Long comercioId,
                                                     EstadoPedido estado, boolean sinRepartidor) {
        var alcance = accesoPedidos.resolver(usuario, Rol.ADMIN, Rol.COMERCIO, Rol.DEPOSITO);
        if (comercioId != null) alcance.validarComercio(comercioId);
        return pedidosRepository.buscarEnAlcance(
                alcance.comercioId() != null ? alcance.comercioId() : comercioId,
                alcance.depositoId(), estado, sinRepartidor).stream()
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

    /**
     * Asigna un repartidor a un pedido.
     * Delegado al patrón State: solo CREADO admite esta transición.
     * Este es el punto único de asignación — RepartidorService debe
     * llamar aquí en lugar de mutar el pedido directamente.
     */
        @Override
    @Transactional
    public PedidoResponseDTO marcarListoParaRetirar(Long id, Usuario usuario) {
        var alcance = accesoPedidos.resolver(usuario, Rol.ADMIN, Rol.COMERCIO, Rol.DEPOSITO);
        Pedido pedido = buscarPedidoParaTransicion(id);
        alcance.validarPedido(pedido);
        validarDepositoDelComercio(pedido.getComercioId(), pedido.getDepositoId());
        resolverEstado.resolver(pedido.getEstado()).marcarListoParaRetirar(pedido);
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.LISTO_PARA_RETIRAR);
        // Mensajeria asincronica: el evento se publica al broker recien cuando la
        // transaccion hace commit (ver PedidoDespachoPublisher).
        eventPublisher.publishEvent(new PedidoListoParaRetirarEvent(pedido.getId()));
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO asignarRepartidor(Long pedidoId, Long repartidorId) {
        Pedido pedido = buscarPedidoParaAsignar(pedidoId);
        Repartidor repartidor = repartidorRepository.findByIdForUpdate(repartidorId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Repartidor con id=" + repartidorId + " no encontrado"));

        if (pedido.getRepartidor() != null) {
            throw new IllegalStateException("El pedido ya tiene un repartidor asignado");
        }

        if (!repartidor.isActivo()) {
            throw new IllegalStateException("No se puede asignar el pedido a un repartidor inactivo");
        }

        if (repartidor.getEstado() != EstadoRepartidor.DISPONIBLE) {
            throw new IllegalStateException(
                    "El repartidor debe estar DISPONIBLE para tomar o recibir un pedido");
        }

        if (pedidosRepository.existsByRepartidorIdAndEstadoIn(repartidor.getId(), ESTADOS_ACTIVOS)) {
            throw new IllegalStateException("El repartidor ya tiene un pedido activo");
        }

        // El estado actual del pedido valida si la transición es legal
        resolverEstado.resolver(pedido.getEstado()).asignarRepartidor(pedido, repartidor);
        repartidor.setEstado(EstadoRepartidor.EN_ENTREGA);

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
        Pedido pedido = buscarPedidoParaTransicion(id);
        validarRepartidorAsignadoOperativo(pedido, null);
        resolverEstado.resolver(pedido.getEstado()).marcarRetirado(pedido);
        pedido.getRepartidor().setEstado(EstadoRepartidor.EN_ENTREGA);
        repartidorRepository.save(pedido.getRepartidor());
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.RETIRADO);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO marcarRetiradoPorRepartidor(Long id, Long repartidorId) {
        Pedido pedido = buscarPedidoParaTransicion(id);
        validarRepartidorAsignadoOperativo(pedido, repartidorId);
        resolverEstado.resolver(pedido.getEstado()).marcarRetirado(pedido);
        pedido.getRepartidor().setEstado(EstadoRepartidor.EN_ENTREGA);
        repartidorRepository.save(pedido.getRepartidor());
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.RETIRADO);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO iniciarViaje(Long id) {
        Pedido pedido = buscarPedidoParaTransicion(id);
        validarRepartidorAsignadoOperativo(pedido, null);
        resolverEstado.resolver(pedido.getEstado()).iniciarViaje(pedido);
        pedido.getRepartidor().setEstado(EstadoRepartidor.EN_ENTREGA);
        repartidorRepository.save(pedido.getRepartidor());
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.EN_CAMINO);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO iniciarViajePorRepartidor(Long id, Long repartidorId) {
        Pedido pedido = buscarPedidoParaTransicion(id);
        validarRepartidorAsignadoOperativo(pedido, repartidorId);
        resolverEstado.resolver(pedido.getEstado()).iniciarViaje(pedido);
        pedido.getRepartidor().setEstado(EstadoRepartidor.EN_ENTREGA);
        repartidorRepository.save(pedido.getRepartidor());
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
        Pedido pedido = buscarPedidoParaTransicion(id);
        validarRepartidorAsignadoOperativo(pedido, null);
        resolverEstado.resolver(pedido.getEstado()).entregar(pedido);
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.ENTREGADO);
        liberarRepartidorSiCorresponde(pedido);
        return toResponseDTO(pedido);
    }

    @Override
    @Transactional
    public PedidoResponseDTO entregarPorRepartidor(Long id, Long repartidorId) {
        Pedido pedido = buscarPedidoParaTransicion(id);
        validarRepartidorAsignadoOperativo(pedido, repartidorId);
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
    public PedidoResponseDTO cancelar(Long id, Usuario usuario) {
        var alcance = accesoPedidos.resolver(usuario, Rol.ADMIN, Rol.COMERCIO);
        Pedido pedido = buscarPedidoParaTransicion(id);
        alcance.validarPedido(pedido);
        validarEntidadesParaRestaurar(pedido);
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

    private Pedido buscarPedidoParaAsignar(Long id) {
        return pedidosRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido con id=" + id + " no encontrado"));
    }

    private Pedido buscarPedidoParaTransicion(Long id) {
        return pedidosRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido con id=" + id + " no encontrado"));
    }

    private Repartidor validarRepartidorAsignadoOperativo(Pedido pedido, Long repartidorEsperadoId) {
        if (pedido.getRepartidor() == null || pedido.getRepartidor().getId() == null) {
            throw new IllegalStateException("El pedido no tiene un repartidor asignado");
        }

        Repartidor repartidor = repartidorRepository.findByIdForUpdate(pedido.getRepartidor().getId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Repartidor con id=" + pedido.getRepartidor().getId() + " no encontrado"));

        if (repartidorEsperadoId != null && !repartidor.getId().equals(repartidorEsperadoId)) {
            throw new AccessDeniedException("El pedido no pertenece al repartidor autenticado");
        }

        if (!repartidor.isActivo()) {
            throw new IllegalStateException("El repartidor asignado está inactivo");
        }

        if (repartidor.getEstado() != EstadoRepartidor.EN_ENTREGA) {
            throw new IllegalStateException(
                    "El repartidor debe estar EN_ENTREGA para operar el pedido");
        }

        pedido.setRepartidor(repartidor);
        return repartidor;
    }

    private void registrarHistorial(Pedido pedido, EstadoPedido estado) {
        HistorialEstadoPedido historial = HistorialEstadoPedido.builder()
                .pedido(pedido)
                .estado(EstadoHistoricoPedido.desdeOperativo(estado))
                .fechaHora(LocalDateTime.now())
                .build();
        historialRepository.save(historial);
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
                    .sorted(Comparator.comparing(HistorialEstadoPedido::getFechaHora).reversed())
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
            // Manejo defensivo en caso de inconsistencia de datos
        }
    }

    private Usuario buscarUsuario(Long usuarioId) {
        if (usuarioId == null) return null;
        return usuarioRepository.findById(usuarioId).orElse(null);
    }

    private void validarDepositoDelComercio(Long comercioId, Long depositoId) {
        if (comercioId == null || depositoId == null) {
            throw new IllegalArgumentException("Comercio y deposito son obligatorios");
        }
        var deposito = depositoRepository.findById(depositoId)
                .orElseThrow(() -> new NoSuchElementException("Deposito no encontrado"));
        validarComercioRelacionado(comercioId, deposito.getComercio() == null ? null : deposito.getComercio().getId());
    }

    private void validarComercioRelacionado(Long esperado, Long real) {
        if (esperado == null || !esperado.equals(real)) {
            throw new AccessDeniedException("Las entidades no pertenecen al comercio autorizado");
        }
    }

    private void validarItemDelComercio(ItemInventario item, Long comercioId) {
        validarComercioRelacionado(comercioId, item.getInventario().getComercio() == null
                ? null : item.getInventario().getComercio().getId());
        validarComercioRelacionado(comercioId, item.getProducto().getComercio() == null
                ? null : item.getProducto().getComercio().getId());
        validarComercioRelacionado(comercioId, item.getDeposito().getComercio() == null
                ? null : item.getDeposito().getComercio().getId());
    }

    private void validarEntidadesParaRestaurar(Pedido pedido) {
        validarDepositoDelComercio(pedido.getComercioId(), pedido.getDepositoId());
        if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) return;
        Inventario inv = inventarioService.obtenerPorComercio(pedido.getComercioId());
        validarComercioRelacionado(pedido.getComercioId(), inv.getComercio() == null ? null : inv.getComercio().getId());
        for (DetallePedido detalle : pedido.getDetalles()) {
            validarComercioRelacionado(pedido.getComercioId(), detalle.getProducto().getComercio() == null
                    ? null : detalle.getProducto().getComercio().getId());
            itemInventarioRepository.findByInventarioIdAndProductoIdAndDepositoId(
                    inv.getId(), detalle.getProducto().getId(), pedido.getDepositoId())
                    .ifPresent(item -> validarItemDelComercio(item, pedido.getComercioId()));
        }
    }
}
