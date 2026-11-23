package com.example.DA2Back.mensajeria;

import com.example.DA2Back.mensajeria.excepcion.SinRepartidorDisponibleException;
import com.example.DA2Back.mensajeria.negocio.AsignadorAutomaticoPedidos;
import com.example.DA2Back.mensajeria.negocio.PublicadorPedidosListos;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import com.example.DA2Back.pedidos.excepcion.TransicionInvalidaPedidoException;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import com.example.DA2Back.pedidos.negocio.evento.PedidoListoParaRetirarEvent;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
import jakarta.jms.Message;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

/**
 * Productor y consumidor de la cola de pedidos listos (Eje C): idempotencia del consumidor,
 * reintento con el siguiente repartidor y propagacion del error para que el broker aplique
 * redelivery y, agotado, envie el mensaje a la DLQ.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MensajeriaPedidosUnitTest {

    @Mock private PedidosRepository pedidosRepository;
    @Mock private RepartidorRepository repartidorRepository;
    @Mock private ServicioDePedidos servicioDePedidos;
    @Mock private JmsTemplate jmsTemplate;

    // ═════════════════ consumidor ═════════════════

    @Test
    @DisplayName("Mensaje de un pedido inexistente se descarta sin error ni asignacion")
    void consumidor_pedidoInexistente() {
        when(pedidosRepository.findById(5L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> consumidor().alRecibirPedidoListo("5"));

        verify(servicioDePedidos, never()).asignarRepartidor(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Idempotencia: pedido ya asignado o fuera de LISTO_PARA_RETIRAR se descarta (mensaje duplicado)")
    void consumidor_mensajeDuplicado() {
        Pedido yaAsignado = pedido(5L, EstadoPedido.ASIGNADO, repartidor(1L));
        when(pedidosRepository.findById(5L)).thenReturn(Optional.of(yaAsignado));
        consumidor().alRecibirPedidoListo("5");

        Pedido conRepartidorPeroListo = pedido(6L, EstadoPedido.LISTO_PARA_RETIRAR, repartidor(1L));
        when(pedidosRepository.findById(6L)).thenReturn(Optional.of(conRepartidorPeroListo));
        consumidor().alRecibirPedidoListo("6");

        Pedido cancelado = pedido(7L, EstadoPedido.CANCELADO, null);
        when(pedidosRepository.findById(7L)).thenReturn(Optional.of(cancelado));
        consumidor().alRecibirPedidoListo("7");

        verify(servicioDePedidos, never()).asignarRepartidor(anyLong(), anyLong());
        verify(repartidorRepository, never()).findByActivoTrueAndEstadoOrderByIdAsc(any());
    }

    @Test
    @DisplayName("Asigna al primer repartidor disponible y no prueba con los demas; tolera espacios en el cuerpo")
    void consumidor_asignaAlPrimero() {
        when(pedidosRepository.findById(5L)).thenReturn(Optional.of(pedido(5L, EstadoPedido.LISTO_PARA_RETIRAR, null)));
        when(repartidorRepository.findByActivoTrueAndEstadoOrderByIdAsc(EstadoRepartidor.DISPONIBLE))
                .thenReturn(List.of(repartidor(1L), repartidor(2L)));

        consumidor().alRecibirPedidoListo(" 5 ");

        verify(servicioDePedidos).asignarRepartidor(5L, 1L);
        verify(servicioDePedidos, never()).asignarRepartidor(5L, 2L);
    }

    @Test
    @DisplayName("Si un candidato no puede tomar el pedido (estado, transicion o inexistente) prueba con el siguiente")
    void consumidor_reintentaConElSiguiente() {
        when(pedidosRepository.findById(5L)).thenReturn(Optional.of(pedido(5L, EstadoPedido.LISTO_PARA_RETIRAR, null)));
        when(repartidorRepository.findByActivoTrueAndEstadoOrderByIdAsc(EstadoRepartidor.DISPONIBLE))
                .thenReturn(List.of(repartidor(1L), repartidor(2L), repartidor(3L), repartidor(4L)));
        when(servicioDePedidos.asignarRepartidor(5L, 1L)).thenThrow(new IllegalStateException("ocupado"));
        when(servicioDePedidos.asignarRepartidor(5L, 2L)).thenThrow(new TransicionInvalidaPedidoException("x"));
        when(servicioDePedidos.asignarRepartidor(5L, 3L)).thenThrow(new NoSuchElementException("no existe"));

        consumidor().alRecibirPedidoListo("5");

        InOrder orden = inOrder(servicioDePedidos);
        orden.verify(servicioDePedidos).asignarRepartidor(5L, 1L);
        orden.verify(servicioDePedidos).asignarRepartidor(5L, 2L);
        orden.verify(servicioDePedidos).asignarRepartidor(5L, 3L);
        orden.verify(servicioDePedidos).asignarRepartidor(5L, 4L);
    }

    @Test
    @DisplayName("Sin repartidores o con todos rechazando -> SinRepartidorDisponible (el broker reintenta y luego DLQ)")
    void consumidor_sinRepartidorDisponible() {
        when(pedidosRepository.findById(5L)).thenReturn(Optional.of(pedido(5L, EstadoPedido.LISTO_PARA_RETIRAR, null)));
        when(repartidorRepository.findByActivoTrueAndEstadoOrderByIdAsc(EstadoRepartidor.DISPONIBLE))
                .thenReturn(List.of());
        assertThrows(SinRepartidorDisponibleException.class, () -> consumidor().alRecibirPedidoListo("5"));

        when(repartidorRepository.findByActivoTrueAndEstadoOrderByIdAsc(EstadoRepartidor.DISPONIBLE))
                .thenReturn(List.of(repartidor(1L)));
        when(servicioDePedidos.asignarRepartidor(5L, 1L)).thenThrow(new IllegalStateException("ocupado"));
        assertThrows(SinRepartidorDisponibleException.class, () -> consumidor().alRecibirPedidoListo("5"));
    }

    @Test
    @DisplayName("Mensaje con cuerpo no numerico (poison message) lanza excepcion para ir a la DLQ")
    void consumidor_mensajeInvalido() {
        assertThrows(NumberFormatException.class, () -> consumidor().alRecibirPedidoListo("no-es-un-id"));
        verify(pedidosRepository, never()).findById(any());
    }

    // ═════════════════ productor ═════════════════

    @Test
    @DisplayName("Publica en la cola configurada un mensaje de texto con el id y las propiedades del evento")
    void productor_publicaMensaje() throws Exception {
        new PublicadorPedidosListos(jmsTemplate, "cola.pedidos")
                .alConfirmarseElPedido(new PedidoListoParaRetirarEvent(42L));

        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq("cola.pedidos"), captor.capture());
        Session session = mock(Session.class);
        TextMessage texto = mock(TextMessage.class);
        when(session.createTextMessage("42")).thenReturn(texto);

        Message creado = captor.getValue().createMessage(session);

        assertSame(texto, creado);
        verify(texto).setLongProperty("pedidoId", 42L);
        verify(texto).setStringProperty("tipoEvento", "PEDIDO_LISTO_PARA_RETIRAR");
    }

    @Test
    @DisplayName("Si el broker esta caido el productor NO propaga el error: el pedido queda LISTO_PARA_RETIRAR")
    void productor_brokerCaido_noPropaga() {
        doThrow(new UncategorizedJmsException("broker caido")).when(jmsTemplate).send(anyStr(), any(MessageCreator.class));

        assertDoesNotThrow(() -> new PublicadorPedidosListos(jmsTemplate, "cola.pedidos")
                .alConfirmarseElPedido(new PedidoListoParaRetirarEvent(42L)));

        verify(jmsTemplate).send(eq("cola.pedidos"), any(MessageCreator.class));
    }

    // ───────────── helpers ─────────────

    private static String anyStr() {
        return org.mockito.ArgumentMatchers.anyString();
    }

    private AsignadorAutomaticoPedidos consumidor() {
        return new AsignadorAutomaticoPedidos(pedidosRepository, repartidorRepository, servicioDePedidos);
    }

    private Pedido pedido(Long id, EstadoPedido estado, Repartidor repartidor) {
        return Pedido.builder().id(id).estado(estado).repartidor(repartidor).build();
    }

    private Repartidor repartidor(Long id) {
        return Repartidor.builder().id(id).usuarioId(id).estado(EstadoRepartidor.DISPONIBLE).activo(true).build();
    }
}
