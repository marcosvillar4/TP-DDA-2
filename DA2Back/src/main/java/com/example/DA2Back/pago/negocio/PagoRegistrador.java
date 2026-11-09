package com.example.DA2Back.pago.negocio;

import com.example.DA2Back.pago.dato.EstadoPago;
import com.example.DA2Back.pago.dato.Pago;
import com.example.DA2Back.pago.dato.PagoRepository;
import com.example.DA2Back.pedidos.dato.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Altas y anulaciones idempotentes de pagos. Cada operacion corre en su propia
 * transaccion: si dos requests crean el mismo pago a la vez (unique en pedido_id),
 * solo falla esa operacion y no arrastra la consulta que la origino.
 */
@Component
@RequiredArgsConstructor
public class PagoRegistrador {

    private final PagoRepository pagos;

    /** Crea el pago PENDIENTE del pedido si todavia no existe. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void asegurarPendiente(Pedido pedido) {
        if (pagos.existsByPedidoId(pedido.getId())) {
            return;
        }
        pagos.saveAndFlush(Pago.builder()
                .pedidoId(pedido.getId())
                .comercioId(pedido.getComercioId())
                .monto(pedido.getCostoEnvio())
                .estado(EstadoPago.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build());
    }

    /** Anula el pago si sigue PENDIENTE (un pago ya cobrado nunca se anula aca). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void anularSiPendiente(Long pagoId) {
        pagos.findByIdForUpdate(pagoId)
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .ifPresent(p -> {
                    p.setEstado(EstadoPago.ANULADO);
                    pagos.save(p);
                });
    }
}
