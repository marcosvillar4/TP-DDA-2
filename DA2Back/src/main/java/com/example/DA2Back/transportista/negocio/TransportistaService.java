package com.example.DA2Back.transportista.negocio;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.dato.TipoEnvio;
import com.example.DA2Back.transportista.dato.EstadoTransportista;
import com.example.DA2Back.transportista.dato.Transportista;
import com.example.DA2Back.transportista.dato.TransportistaRepository;
import com.example.DA2Back.transportista.dto.CambiarEstadoTransportistaDTO;
import com.example.DA2Back.transportista.dto.TransportistaCreateDTO;
import com.example.DA2Back.transportista.dto.TransportistaResponseDTO;
import com.example.DA2Back.transportista.dto.TransportistaUpdateDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransportistaService implements ServicioDeTransportistas {

    private static final List<EstadoPedido> ESTADOS_ACTIVOS =
            List.of(EstadoPedido.CREADO, EstadoPedido.LISTO_PARA_RETIRAR,
                    EstadoPedido.ASIGNADO, EstadoPedido.RETIRADO, EstadoPedido.EN_CAMINO);

    private final TransportistaRepository transportistaRepository;
    private final PedidosRepository pedidosRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TransportistaResponseDTO> listarTodos() {
        return transportistaRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransportistaResponseDTO obtenerPorId(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional
    public TransportistaResponseDTO crear(TransportistaCreateDTO dto) {
        String empresa = normalizar(dto == null ? null : dto.getEmpresa());
        if (transportistaRepository.existsByEmpresaIgnoreCase(empresa)) {
            throw new IllegalStateException("Ya existe un transportista con la empresa indicada");
        }
        return toResponse(transportistaRepository.save(Transportista.builder()
                .empresa(empresa)
                .estado(EstadoTransportista.DISPONIBLE)
                .activo(true)
                .build()));
    }

    @Override
    @Transactional
    public TransportistaResponseDTO actualizar(Long id, TransportistaUpdateDTO dto) {
        Transportista transportista = buscar(id);
        String empresa = normalizar(dto == null ? null : dto.getEmpresa());
        if (!transportista.getEmpresa().equalsIgnoreCase(empresa)
                && transportistaRepository.existsByEmpresaIgnoreCase(empresa)) {
            throw new IllegalStateException("Ya existe un transportista con la empresa indicada");
        }
        transportista.setEmpresa(empresa);
        return toResponse(transportistaRepository.save(transportista));
    }

    @Override
    @Transactional
    public TransportistaResponseDTO cambiarEstado(Long id, CambiarEstadoTransportistaDTO dto) {
        if (dto == null || dto.getEstado() == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        Transportista transportista = buscar(id);
        if (!transportista.isActivo()) {
            throw new IllegalStateException("No se puede cambiar el estado de un transportista inactivo");
        }
        if (dto.getEstado() == EstadoTransportista.DISPONIBLE
                && tienePedidoActivo(transportista.getId())) {
            throw new IllegalStateException("El transportista tiene un pedido activo");
        }
        transportista.setEstado(dto.getEstado());
        return toResponse(transportistaRepository.save(transportista));
    }

    @Override
    @Transactional
    public TransportistaResponseDTO desactivar(Long id) {
        Transportista transportista = buscar(id);
        if (tienePedidoActivo(id)) {
            throw new IllegalStateException("El transportista tiene un pedido activo");
        }
        transportista.setActivo(false);
        transportista.setEstado(EstadoTransportista.NO_DISPONIBLE);
        return toResponse(transportistaRepository.save(transportista));
    }

    @Override
    @Transactional
    public TransportistaResponseDTO activar(Long id) {
        Transportista transportista = buscar(id);
        transportista.setActivo(true);
        transportista.setEstado(EstadoTransportista.DISPONIBLE);
        return toResponse(transportistaRepository.save(transportista));
    }

    @Override
    public void asignarParaPedido(Pedido pedido) {
        Transportista transportista = transportistaRepository
                .findFirstByActivoTrueAndEstadoOrderByIdAsc(EstadoTransportista.DISPONIBLE)
                .orElseThrow(() -> new IllegalStateException(
                        "No hay transportistas externos disponibles"));
        transportista.setEstado(EstadoTransportista.EN_SERVICIO);
        pedido.setTransportista(transportista);
        pedido.setCodigoEnvio("ENV-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        pedido.setCodigoSeguimiento("TRK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase());
        transportistaRepository.save(transportista);
    }

    @Override
    public void liberarSiCorresponde(Pedido pedido) {
        if (pedido.getTipoEnvio() != TipoEnvio.EXTERNO || pedido.getTransportista() == null) return;
        if (pedido.getEstado() != EstadoPedido.ENTREGADO && pedido.getEstado() != EstadoPedido.CANCELADO) return;
        Transportista transportista = transportistaRepository.findById(pedido.getTransportista().getId()).orElse(null);
        if (transportista != null && transportista.isActivo()) {
            transportista.setEstado(EstadoTransportista.DISPONIBLE);
            transportistaRepository.save(transportista);
        }
    }

    private boolean tienePedidoActivo(Long transportistaId) {
        return pedidosRepository.existsByTransportistaIdAndEstadoIn(transportistaId, ESTADOS_ACTIVOS);
    }

    private Transportista buscar(Long id) {
        return transportistaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transportista con id=" + id + " no encontrado"));
    }

    private String normalizar(String empresa) {
        if (empresa == null || empresa.trim().isEmpty()) {
            throw new IllegalArgumentException("La empresa es obligatoria");
        }
        return empresa.trim();
    }

    private TransportistaResponseDTO toResponse(Transportista transportista) {
        return TransportistaResponseDTO.builder()
                .id(transportista.getId())
                .empresa(transportista.getEmpresa())
                .estado(transportista.getEstado())
                .activo(transportista.isActivo())
                .build();
    }
}
