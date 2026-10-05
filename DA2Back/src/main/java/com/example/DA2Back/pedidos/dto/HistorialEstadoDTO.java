package com.example.DA2Back.pedidos.dto;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialEstadoDTO {
    private Long id;
    private EstadoPedido estado;
    private LocalDateTime fechaHora;
}
