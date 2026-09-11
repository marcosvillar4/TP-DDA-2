package com.example.DA2Back.repartidor.negocio;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import com.example.DA2Back.repartidor.dto.CambiarEstadoRepartidorDTO;
import com.example.DA2Back.repartidor.dto.EntregaHistorialDTO;
import com.example.DA2Back.repartidor.dto.PedidoActualDTO;
import com.example.DA2Back.repartidor.dto.RepartidorCreateDTO;
import com.example.DA2Back.repartidor.dto.RepartidorMapper;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.dto.RepartidorUpdateDTO;
import com.example.DA2Back.repartidor.dto.UsuarioRepartidorDisponibleDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RepartidorService implements IRepartidorService {

    private static final List<EstadoPedido> ESTADOS_PEDIDO_ACTIVO =
            List.of(EstadoPedido.ASIGNADO, EstadoPedido.EN_CAMINO);

    private static final List<EstadoPedido> ESTADOS_PEDIDO_FINALIZADO =
            List.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO);

    private final RepartidorRepository repartidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidosRepository pedidosRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RepartidorResponseDTO> listarTodos() {
        return repartidorRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RepartidorResponseDTO obtenerPorId(Long id) {
        return toResponseDTO(buscarRepartidor(id));
    }

    @Override
    @Transactional
    public RepartidorResponseDTO crear(RepartidorCreateDTO dto) {
        validarCamposCreacion(dto);

        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Usuario con id=" + dto.getUsuarioId() + " no encontrado"));

        if (usuario.getRol() != Rol.REPARTIDOR) {
            throw new IllegalArgumentException("El usuario indicado no tiene rol REPARTIDOR");
        }

        if (repartidorRepository.existsByUsuarioId(usuario.getId())) {
            throw new IllegalStateException("El usuario ya está asociado a un repartidor");
        }

        if (repartidorRepository.existsByPatenteIgnoreCase(dto.getPatente().trim())) {
            throw new IllegalStateException("Ya existe un repartidor con la patente indicada");
        }

        Repartidor repartidor = RepartidorMapper.toEntity(dto, usuario);
        return toResponseDTO(repartidorRepository.save(repartidor));
    }

    @Override
    @Transactional
    public RepartidorResponseDTO actualizar(Long id, RepartidorUpdateDTO dto) {
        validarCamposActualizacion(dto);
        Repartidor repartidor = buscarRepartidor(id);

        String patente = dto.getPatente().trim();
        if (!repartidor.getPatente().equalsIgnoreCase(patente)
                && repartidorRepository.existsByPatenteIgnoreCase(patente)) {
            throw new IllegalStateException("Ya existe un repartidor con la patente indicada");
        }

        RepartidorMapper.applyUpdate(repartidor, dto);
        return toResponseDTO(repartidorRepository.save(repartidor));
    }

    @Override
    @Transactional
    public RepartidorResponseDTO cambiarEstado(Long id, CambiarEstadoRepartidorDTO dto) {
        if (dto.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }

        Repartidor repartidor = buscarRepartidor(id);
        if (!repartidor.isActivo()) {
            throw new IllegalStateException("No se puede cambiar el estado de un repartidor inactivo");
        }

        validarCambioEstadoManual(repartidor, dto.getEstado());

        repartidor.setEstado(dto.getEstado());
        return toResponseDTO(repartidorRepository.save(repartidor));
    }

    @Override
    @Transactional
    public RepartidorResponseDTO desactivar(Long id) {
        Repartidor repartidor = buscarRepartidor(id);
        validarSinPedidoActivo(repartidor);
        repartidor.setActivo(false);
        repartidor.setEstado(EstadoRepartidor.NO_DISPONIBLE);
        return toResponseDTO(repartidorRepository.save(repartidor));
    }

    @Override
    @Transactional
    public RepartidorResponseDTO activar(Long id) {
        Repartidor repartidor = buscarRepartidor(id);
        repartidor.setActivo(true);
        if (!tienePedidoActivo(repartidor.getId())) {
            repartidor.setEstado(EstadoRepartidor.DISPONIBLE);
        }
        return toResponseDTO(repartidorRepository.save(repartidor));
    }

    @Override
    @Transactional
    public RepartidorResponseDTO asignarPedido(Long repartidorId, Long pedidoId) {
        Repartidor repartidor = buscarRepartidor(repartidorId);

        if (!repartidor.isActivo()) {
            throw new IllegalStateException("No se puede asignar un pedido a un repartidor inactivo");
        }

        if (repartidor.getEstado() != EstadoRepartidor.DISPONIBLE) {
            throw new IllegalStateException("El repartidor no está disponible para recibir pedidos");
        }

        Pedido pedido = pedidosRepository.findById(pedidoId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Pedido con id=" + pedidoId + " no encontrado"));

        if (pedido.getRepartidor() != null) {
            throw new IllegalStateException("El pedido ya tiene un repartidor asignado");
        }

        if (pedido.getEstado() != EstadoPedido.CREADO) {
            throw new IllegalStateException("Solo se pueden asignar pedidos en estado CREADO");
        }

        pedido.setRepartidor(repartidor);
        pedido.setEstado(EstadoPedido.ASIGNADO);
        repartidor.setEstado(EstadoRepartidor.EN_ENTREGA);

        pedidosRepository.save(pedido);
        return toResponseDTO(repartidorRepository.save(repartidor));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntregaHistorialDTO> obtenerHistorial(Long repartidorId) {
        buscarRepartidor(repartidorId);
        return pedidosRepository.findByRepartidorIdAndEstadoIn(
                        repartidorId,
                        ESTADOS_PEDIDO_FINALIZADO
                )
                .stream()
                .map(pedido -> EntregaHistorialDTO.builder()
                        .fecha(null)
                        .pedidoId(pedido.getId())
                        .direccionEntrega(pedido.getDireccionDestino())
                        .resultado(pedido.getEstado())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PedidoResponseDTO> obtenerPedidosAsignables() {
        return pedidosRepository.findByEstadoAndRepartidorIsNull(EstadoPedido.CREADO)
                .stream()
                .map(this::toPedidoResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRepartidorDisponibleDTO> listarUsuariosRepartidoresDisponibles() {
        return usuarioRepository.findAll()
                .stream()
                .filter(usuario -> usuario.getRol() == Rol.REPARTIDOR)
                .filter(usuario -> !repartidorRepository.existsByUsuarioId(usuario.getId()))
                .map(usuario -> UsuarioRepartidorDisponibleDTO.builder()
                        .id(usuario.getId())
                        .username(usuario.getNombreUsuario())
                        .email(usuario.getEmail())
                        .build())
                .toList();
    }

    private Repartidor buscarRepartidor(Long id) {
        return repartidorRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Repartidor con id=" + id + " no encontrado"));
    }

    private RepartidorResponseDTO toResponseDTO(Repartidor repartidor) {
        return RepartidorMapper.toResponseDTO(
                repartidor,
                buscarPedidoActual(repartidor.getId())
        );
    }

    private PedidoActualDTO buscarPedidoActual(Long repartidorId) {
        return pedidosRepository.findFirstByRepartidorIdAndEstadoIn(
                        repartidorId,
                        ESTADOS_PEDIDO_ACTIVO
                )
                .map(pedido -> PedidoActualDTO.builder()
                        .id(pedido.getId())
                        .estado(pedido.getEstado())
                        .direccionDestino(pedido.getDireccionDestino())
                        .build())
                .orElse(null);
    }

    private PedidoResponseDTO toPedidoResponseDTO(Pedido pedido) {
        Long repartidorId = pedido.getRepartidor() != null ? pedido.getRepartidor().getId() : null;
        String repartidorNombre = pedido.getRepartidor() != null
                ? pedido.getRepartidor().getNombre() + " " + pedido.getRepartidor().getApellido()
                : null;

        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .comercioId(pedido.getComercioId())
                .direccionDestino(pedido.getDireccionDestino())
                .estado(pedido.getEstado())
                .repartidorId(repartidorId)
                .repartidorNombre(repartidorNombre)
                .build();
    }

    private boolean tienePedidoActivo(Long repartidorId) {
        return pedidosRepository.existsByRepartidorIdAndEstadoIn(
                repartidorId,
                ESTADOS_PEDIDO_ACTIVO
        );
    }

    private void validarCambioEstadoManual(Repartidor repartidor, EstadoRepartidor nuevoEstado) {
        if (nuevoEstado == EstadoRepartidor.EN_ENTREGA) {
            throw new IllegalStateException(
                    "El estado EN_ENTREGA se asigna automáticamente al asociar un pedido activo");
        }

        validarSinPedidoActivo(repartidor);
    }

    private void validarSinPedidoActivo(Repartidor repartidor) {
        if (tienePedidoActivo(repartidor.getId())) {
            throw new IllegalStateException(
                    "El repartidor tiene un pedido activo y no puede cambiar su disponibilidad");
        }
    }

    private void validarCamposCreacion(RepartidorCreateDTO dto) {
        if (dto.getUsuarioId() == null) {
            throw new IllegalArgumentException("El usuario es obligatorio");
        }
        validarObligatorio(dto.getNombre(), "El nombre es obligatorio");
        validarObligatorio(dto.getApellido(), "El apellido es obligatorio");
        validarObligatorio(dto.getTelefono(), "El teléfono es obligatorio");
        validarObligatorio(dto.getTipoVehiculo(), "El tipo de vehículo es obligatorio");
        validarObligatorio(dto.getPatente(), "La patente es obligatoria");
        validarObligatorio(dto.getZona(), "La zona es obligatoria");
    }

    private void validarCamposActualizacion(RepartidorUpdateDTO dto) {
        validarObligatorio(dto.getNombre(), "El nombre es obligatorio");
        validarObligatorio(dto.getApellido(), "El apellido es obligatorio");
        validarObligatorio(dto.getTelefono(), "El teléfono es obligatorio");
        validarObligatorio(dto.getTipoVehiculo(), "El tipo de vehículo es obligatorio");
        validarObligatorio(dto.getPatente(), "La patente es obligatoria");
        validarObligatorio(dto.getZona(), "La zona es obligatoria");
    }

    private void validarObligatorio(String valor, String mensaje) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}
