package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedido;
import com.example.DA2Back.pedidos.dato.HistorialEstadoPedidoRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
            List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO);

    private final PedidosRepository pedidosRepository;
    private final RepartidorRepository repartidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialEstadoPedidoRepository historialRepository;
    private final ResolverEstadoPedido resolverEstado;

    // -------------------------------------------------------------------------
    // Creación
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public PedidoResponseDTO crearPedido(CrearPedidoDTO dto) {
        Pedido pedido = Pedido.builder()
                .comercioId(dto.getComercioId())
                .direccionDestino(dto.getDireccionDestino())
                .direccionOrigen(dto.getDireccionOrigen())
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE_COTIZACION)
                .build();

        Pedido guardado = pedidosRepository.save(pedido);
        registrarHistorial(guardado, EstadoPedido.PENDIENTE_COTIZACION);
        return toResponseDTO(guardado);
    }

    // -------------------------------------------------------------------------
    // Consultas
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public PedidoResponseDTO obtenerPorId(Long id) {
        return toResponseDTO(buscarPedido(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarTodos() {
        return pedidosRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> listarPorComercio(Long comercioId) {
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
        return pedidosRepository.findByEstadoAndRepartidorIsNull(EstadoPedido.PENDIENTE_COTIZACION).stream()
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
    public PedidoResponseDTO asignarRepartidor(Long pedidoId, Long repartidorId) {
        Pedido pedido = buscarPedido(pedidoId);
        Repartidor repartidor = repartidorRepository.findById(repartidorId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Repartidor con id=" + repartidorId + " no encontrado"));

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
    public PedidoResponseDTO iniciarViaje(Long id) {
        Pedido pedido = buscarPedido(id);
        resolverEstado.resolver(pedido.getEstado()).iniciarViaje(pedido);
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
        resolverEstado.resolver(pedido.getEstado()).entregar(pedido);
        pedidosRepository.save(pedido);
        registrarHistorial(pedido, EstadoPedido.ENTREGADO);
        liberarRepartidorSiCorresponde(pedido);
        return toResponseDTO(pedido);
    }

    /**
     * Cancela el pedido desde cualquier estado que lo permita.
     * Libera al repartidor si estaba asignado.
     */
    @Override
    @Transactional
    public PedidoResponseDTO cancelar(Long id) {
        Pedido pedido = buscarPedido(id);
        resolverEstado.resolver(pedido.getEstado()).cancelar(pedido);
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
        if (pedido.getRepartidor() != null) {
            Usuario usuario = buscarUsuario(pedido.getRepartidor().getUsuarioId());
            repartidorNombre = usuario.getNombre() + " " + usuario.getApellido();
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

        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .comercioId(pedido.getComercioId())
                .direccionDestino(pedido.getDireccionDestino())
                .direccionOrigen(pedido.getDireccionOrigen())
                .fechaCreacion(pedido.getFechaCreacion())
                .estado(pedido.getEstado())
                .repartidorId(repartidorId)
                .repartidorNombre(repartidorNombre)
                .historial(historialDTO)
                .build();
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Usuario con id=" + usuarioId + " no encontrado"));
    }
}
