package com.example.DA2Back.notificacion.dto;

import com.example.DA2Back.notificacion.dato.CanalNotificacion;
import com.example.DA2Back.notificacion.dato.TipoNotificacion;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificacionResponseDTO {
    private Long id;
    private TipoNotificacion tipo;
    private CanalNotificacion canal;
    private String titulo;
    private String mensaje;
    private Long referenciaId;
    private EstadoPedido estado;
    private String estadoLabel;
    private boolean leida;
    private boolean enviada;
    private LocalDateTime fechaCreacion;
}
