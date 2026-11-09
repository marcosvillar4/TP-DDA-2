package com.example.DA2Back.pago.negocio;

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
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pagos y cobranzas del costo de envio.
 *
 * Los pagos se generan solos a partir de los pedidos con costo de envio: al consultar
 * (comercio o repartidor) se crean los que falten y se anulan los PENDIENTES de pedidos
 * cancelados. Asi tambien quedan cubiertos los pedidos creados antes de este modulo.
 *
 * Los metodos de consulta NO son transaccionales a proposito: cada alta/anulacion corre
 * en su propia transaccion (ver PagoRegistrador) y la lectura posterior debe verla.
 */
@Service
@RequiredArgsConstructor
public class PagoService implements IPagoService {

    private final PagoRepository pagoRepository;
    private final PedidosRepository pedidosRepository;
    private final ComercioRepository comercioRepository;
    private final RepartidorRepository repartidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PagoRegistrador registrador;
    private final PasarelaPago pasarela;

    // ───────────────────────────── Comercio ─────────────────────────────

    @Override
    public List<PagoResponseDTO> listarPendientesComercio(Usuario usuario) {
        return listarComercio(usuario, EstadoPago.PENDIENTE);
    }

    @Override
    public List<PagoResponseDTO> listarComercio(Usuario usuario, EstadoPago estado) {
        Comercio comercio = resolverComercio(usuario);
        sincronizar(pedidosRepository.findByComercioId(comercio.getId()));

        List<Pago> pagos = estado == null
                ? pagoRepository.findByComercioIdOrderByFechaCreacionDesc(comercio.getId())
                : pagoRepository.findByComercioIdAndEstadoOrderByFechaCreacionDesc(comercio.getId(), estado);
        return aDTOs(pagos);
    }

    @Override
    public PagoResponseDTO obtenerParaComercio(Long pagoId, Usuario usuario) {
        Comercio comercio = resolverComercio(usuario);
        Pago pago = pagoRepository.findById(pagoId)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado"));
        validarPertenencia(pago, comercio);
        return aDTOs(List.of(pago)).get(0);
    }

    @Override
    @Transactional
    public PagoResponseDTO pagar(Long pagoId, PagarPedidoDTO dto, Usuario usuario) {
        // 1. Quien paga: un COMERCIO habilitado y con comercio asociado.
        Comercio comercio = resolverComercio(usuario);

        if (dto == null || dto.getMetodoPago() == null) {
            throw new IllegalArgumentException("El metodo de pago es obligatorio");
        }

        // 2. El pago existe (con lock para evitar doble cobro concurrente).
        Pago pago = pagoRepository.findByIdForUpdate(pagoId)
                .orElseThrow(() -> new NoSuchElementException("Pago no encontrado"));

        // 3. Es del comercio autenticado.
        validarPertenencia(pago, comercio);

        // 4. Estado del pago: solo se paga lo PENDIENTE.
        if (pago.getEstado() == EstadoPago.PAGADO) {
            throw new IllegalStateException("El pago ya fue realizado");
        }
        if (pago.getEstado() == EstadoPago.ANULADO) {
            throw new IllegalStateException("El pago fue anulado porque el pedido se cancelo");
        }

        // 5. Estado del pedido: no se cobra un envio cancelado.
        Pedido pedido = pedidosRepository.findById(pago.getPedidoId())
                .orElseThrow(() -> new NoSuchElementException("El pedido del pago no existe"));
        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se puede pagar un pedido cancelado");
        }

        // 6. El monto informado debe ser exactamente lo adeudado (no confiamos en el cliente).
        if (dto.getMonto() == null || dto.getMonto().compareTo(pago.getMonto()) != 0) {
            throw new IllegalArgumentException(
                    "El monto no coincide con el importe adeudado (" + pago.getMonto().toPlainString() + ")");
        }

        // 7. Datos del medio de pago.
        MetodoPago metodo = dto.getMetodoPago();
        String numeroTarjeta = null;
        String cbu = null;
        if (metodo.esTarjeta()) {
            numeroTarjeta = ValidadorMedioPago.validarTarjeta(dto.getNumeroTarjeta(), dto.getTitular(),
                    dto.getVencimiento(), dto.getCvv(), YearMonth.now());
        } else {
            cbu = ValidadorMedioPago.validarCbu(dto.getCbu());
        }

        // 8. Pasarela simulada.
        PasarelaPago.ResultadoPasarela resultado = pasarela.procesar(metodo, numeroTarjeta, cbu, pago.getMonto());
        if (!resultado.aprobado()) {
            throw new PagoRechazadoException("Pago rechazado: " + resultado.motivoRechazo());
        }

        // 9. Se confirma. Solo se guardan los ultimos 4 digitos.
        pago.setEstado(EstadoPago.PAGADO);
        pago.setMetodoPago(metodo);
        pago.setReferenciaOperacion(resultado.referencia());
        pago.setTarjetaUltimos4(numeroTarjeta != null ? numeroTarjeta.substring(numeroTarjeta.length() - 4) : null);
        pago.setFechaPago(LocalDateTime.now());
        pago.setPagadoPorUsuarioId(usuario.getId());
        pagoRepository.save(pago);

        return aDTOs(List.of(pago)).get(0);
    }

    // ───────────────────────────── Repartidor ─────────────────────────────

    @Override
    public List<PagoResponseDTO> listarParaRepartidor(Usuario usuario, EstadoPago estado) {
        Repartidor repartidor = resolverRepartidor(usuario);

        List<Pedido> pedidos = pedidosRepository.findByRepartidorIdAndEstadoIn(
                repartidor.getId(), Arrays.asList(EstadoPedido.values()));
        sincronizar(pedidos);

        Set<EstadoPago> estados = estado != null
                ? Set.of(estado)
                : Set.of(EstadoPago.PENDIENTE, EstadoPago.PAGADO); // los anulados no se cobran
        List<Long> pedidoIds = pedidos.stream().map(Pedido::getId).toList();
        if (pedidoIds.isEmpty()) {
            return List.of();
        }
        List<Pago> pagos = pagoRepository.findByPedidoIdIn(pedidoIds).stream()
                .filter(p -> estados.contains(p.getEstado()))
                .sorted(Comparator.comparing(Pago::getFechaCreacion).reversed())
                .toList();
        return aDTOs(pagos);
    }

    // ───────────────────────────── Internos ─────────────────────────────

    /** Crea los pagos faltantes y anula los PENDIENTES de pedidos cancelados. */
    private void sincronizar(List<Pedido> pedidos) {
        if (pedidos.isEmpty()) {
            return;
        }
        Map<Long, Pago> existentes = pagoRepository
                .findByPedidoIdIn(pedidos.stream().map(Pedido::getId).toList()).stream()
                .collect(Collectors.toMap(Pago::getPedidoId, p -> p));

        for (Pedido pedido : pedidos) {
            Pago pago = existentes.get(pedido.getId());
            boolean cancelado = pedido.getEstado() == EstadoPedido.CANCELADO;
            if (pago == null) {
                if (!cancelado && tieneCosto(pedido)) {
                    try {
                        registrador.asegurarPendiente(pedido);
                    } catch (DataIntegrityViolationException e) {
                        // Otro request lo creo en simultaneo: el pago ya existe, es lo esperado.
                    }
                }
            } else if (cancelado && pago.getEstado() == EstadoPago.PENDIENTE) {
                registrador.anularSiPendiente(pago.getId());
            }
        }
    }

    private boolean tieneCosto(Pedido pedido) {
        return pedido.getCostoEnvio() != null && pedido.getCostoEnvio().compareTo(BigDecimal.ZERO) > 0;
    }

    private Comercio resolverComercio(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || !usuario.isEnabled() || usuario.getRol() != Rol.COMERCIO) {
            throw new AccessDeniedException("Solo un comercio habilitado puede operar sus pagos");
        }
        return comercioRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuario sin comercio asociado"));
    }

    private Repartidor resolverRepartidor(Usuario usuario) {
        if (usuario == null || usuario.getId() == null || !usuario.isEnabled() || usuario.getRol() != Rol.REPARTIDOR) {
            throw new AccessDeniedException("Solo un repartidor habilitado puede consultar sus cobros");
        }
        return repartidorRepository.findByUsuarioId(usuario.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuario sin perfil de repartidor"));
    }

    private void validarPertenencia(Pago pago, Comercio comercio) {
        if (!Objects.equals(pago.getComercioId(), comercio.getId())) {
            throw new AccessDeniedException("No tiene acceso al pago solicitado");
        }
    }

    /** Arma los DTOs resolviendo pedidos, comercios y repartidores en lote (sin N+1). */
    private List<PagoResponseDTO> aDTOs(Collection<Pago> pagos) {
        if (pagos.isEmpty()) {
            return List.of();
        }
        Map<Long, Pedido> pedidos = pedidosRepository
                .findAllById(pagos.stream().map(Pago::getPedidoId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Pedido::getId, p -> p));
        Map<Long, Comercio> comercios = comercioRepository
                .findAllById(pagos.stream().map(Pago::getComercioId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Comercio::getId, c -> c));
        Map<Long, Usuario> usuariosRepartidor = usuarioRepository.findAllById(
                pedidos.values().stream()
                        .map(Pedido::getRepartidor)
                        .filter(Objects::nonNull)
                        .map(Repartidor::getUsuarioId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Usuario::getId, u -> u));

        return pagos.stream().map(pago -> {
            Pedido pedido = pedidos.get(pago.getPedidoId());
            Comercio comercio = comercios.get(pago.getComercioId());
            Repartidor repartidor = pedido != null ? pedido.getRepartidor() : null;
            Usuario usuarioRep = repartidor != null ? usuariosRepartidor.get(repartidor.getUsuarioId()) : null;

            return PagoResponseDTO.builder()
                    .id(pago.getId())
                    .monto(pago.getMonto())
                    .estado(pago.getEstado())
                    .metodoPago(pago.getMetodoPago())
                    .referenciaOperacion(pago.getReferenciaOperacion())
                    .tarjetaUltimos4(pago.getTarjetaUltimos4())
                    .fechaCreacion(pago.getFechaCreacion())
                    .fechaPago(pago.getFechaPago())
                    .pedidoId(pago.getPedidoId())
                    .pedidoEstado(pedido != null ? pedido.getEstado() : null)
                    .direccionOrigen(pedido != null ? pedido.getDireccionOrigen() : null)
                    .direccionDestino(pedido != null ? pedido.getDireccionDestino() : null)
                    .comercioId(pago.getComercioId())
                    .comercioNombre(comercio != null ? comercio.getNombreComercial() : null)
                    .repartidorId(repartidor != null ? repartidor.getId() : null)
                    .repartidorNombre(usuarioRep != null ? usuarioRep.getNombre() + " " + usuarioRep.getApellido() : null)
                    .build();
        }).toList();
    }
}
