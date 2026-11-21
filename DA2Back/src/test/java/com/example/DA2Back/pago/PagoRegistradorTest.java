package com.example.DA2Back.pago;

import com.example.DA2Back.pago.dato.EstadoPago;
import com.example.DA2Back.pago.dato.Pago;
import com.example.DA2Back.pago.dato.PagoRepository;
import com.example.DA2Back.pago.negocio.PagoRegistrador;
import com.example.DA2Back.pedidos.dato.Pedido;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PagoRegistradorTest {

    @Mock
    private PagoRepository pagos;

    @InjectMocks
    private PagoRegistrador registrador;

    @Test
    @DisplayName("asegurarPendiente crea un pago PENDIENTE con el costo del pedido")
    void asegurarPendiente_creaPago() {
        Pedido pedido = Pedido.builder().id(7L).comercioId(3L).costoEnvio(new BigDecimal("1200.50")).build();
        when(pagos.existsByPedidoId(7L)).thenReturn(false);

        registrador.asegurarPendiente(pedido);

        ArgumentCaptor<Pago> captor = ArgumentCaptor.forClass(Pago.class);
        verify(pagos).saveAndFlush(captor.capture());
        assertEquals(7L, captor.getValue().getPedidoId());
        assertEquals(3L, captor.getValue().getComercioId());
        assertEquals(new BigDecimal("1200.50"), captor.getValue().getMonto());
        assertEquals(EstadoPago.PENDIENTE, captor.getValue().getEstado());
    }

    @Test
    @DisplayName("asegurarPendiente es idempotente: si ya existe no crea otro")
    void asegurarPendiente_yaExiste_noCrea() {
        when(pagos.existsByPedidoId(7L)).thenReturn(true);

        registrador.asegurarPendiente(Pedido.builder().id(7L).build());

        verify(pagos, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("anularSiPendiente anula un pago PENDIENTE")
    void anular_pendiente() {
        Pago pago = Pago.builder().id(1L).estado(EstadoPago.PENDIENTE).build();
        when(pagos.findByIdForUpdate(1L)).thenReturn(Optional.of(pago));

        registrador.anularSiPendiente(1L);

        assertEquals(EstadoPago.ANULADO, pago.getEstado());
        verify(pagos).save(pago);
    }

    @Test
    @DisplayName("anularSiPendiente nunca anula un pago ya cobrado ni uno inexistente")
    void anular_pagadoOInexistente_noHaceNada() {
        Pago pagado = Pago.builder().id(2L).estado(EstadoPago.PAGADO).build();
        when(pagos.findByIdForUpdate(2L)).thenReturn(Optional.of(pagado));
        when(pagos.findByIdForUpdate(3L)).thenReturn(Optional.empty());

        registrador.anularSiPendiente(2L);
        registrador.anularSiPendiente(3L);

        assertEquals(EstadoPago.PAGADO, pagado.getEstado());
        verify(pagos, never()).save(any());
    }
}
