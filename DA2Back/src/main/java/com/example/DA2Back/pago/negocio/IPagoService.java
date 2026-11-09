package com.example.DA2Back.pago.negocio;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.pago.dato.EstadoPago;
import com.example.DA2Back.pago.dto.PagarPedidoDTO;
import com.example.DA2Back.pago.dto.PagoResponseDTO;

import java.util.List;

public interface IPagoService {

    /** Pagos PENDIENTES de los pedidos del comercio autenticado. */
    List<PagoResponseDTO> listarPendientesComercio(Usuario usuario);

    /** Todos los pagos del comercio autenticado; {@code estado} es un filtro opcional. */
    List<PagoResponseDTO> listarComercio(Usuario usuario, EstadoPago estado);

    PagoResponseDTO obtenerParaComercio(Long pagoId, Usuario usuario);

    /** Paga (simulado) el envio de un pedido del comercio autenticado. */
    PagoResponseDTO pagar(Long pagoId, PagarPedidoDTO dto, Usuario usuario);

    /**
     * Pagos de los pedidos del repartidor autenticado: los cobrados y los pendientes
     * de cobro. Con {@code estado} se filtra por uno solo.
     */
    List<PagoResponseDTO> listarParaRepartidor(Usuario usuario, EstadoPago estado);
}
