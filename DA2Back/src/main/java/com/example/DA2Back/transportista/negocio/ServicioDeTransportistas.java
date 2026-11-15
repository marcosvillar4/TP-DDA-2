package com.example.DA2Back.transportista.negocio;

import java.util.List;

import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.transportista.dto.CambiarEstadoTransportistaDTO;
import com.example.DA2Back.transportista.dto.TransportistaCreateDTO;
import com.example.DA2Back.transportista.dto.TransportistaResponseDTO;
import com.example.DA2Back.transportista.dto.TransportistaUpdateDTO;

public interface ServicioDeTransportistas {
    List<TransportistaResponseDTO> listarTodos();
    TransportistaResponseDTO obtenerPorId(Long id);
    TransportistaResponseDTO crear(TransportistaCreateDTO dto);
    TransportistaResponseDTO actualizar(Long id, TransportistaUpdateDTO dto);
    TransportistaResponseDTO cambiarEstado(Long id, CambiarEstadoTransportistaDTO dto);
    TransportistaResponseDTO desactivar(Long id);
    TransportistaResponseDTO activar(Long id);
    void asignarParaPedido(Pedido pedido);
    void liberarSiCorresponde(Pedido pedido);
}
