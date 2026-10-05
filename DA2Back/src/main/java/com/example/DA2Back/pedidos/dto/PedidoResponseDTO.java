package com.example.DA2Back.pedidos.dto;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO de salida que representa un Pedido en la respuesta HTTP.
 * Garantiza que la entidad JPA nunca sea serializada directamente en la capa web.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PedidoResponseDTO {

    private Long id;
    private Long comercioId;
    private String direccionDestino;
    private String direccionOrigen;
    private java.time.LocalDateTime fechaCreacion;
    private EstadoPedido estado;
    private Long repartidorId;
    private String repartidorNombre;
    private java.util.List<HistorialEstadoDTO> historial;
}
