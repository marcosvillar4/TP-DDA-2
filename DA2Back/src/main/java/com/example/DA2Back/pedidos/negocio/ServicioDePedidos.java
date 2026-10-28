package com.example.DA2Back.pedidos.negocio;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.cotizacion.dto.CotizacionDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import java.util.List;

public interface ServicioDePedidos {
    PedidoResponseDTO crearPedido(CrearPedidoDTO dto, Usuario usuario);
    /** Cotiza un envio sin crear el pedido ni tocar el stock. */
    CotizacionDTO cotizarPedido(CrearPedidoDTO dto, Usuario usuario);
    PedidoResponseDTO obtenerPorId(Long id, Usuario usuario);
    List<PedidoResponseDTO> listarTodos(Usuario usuario);
    List<PedidoResponseDTO> listarPorComercio(Long comercioId, Usuario usuario);
    List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado, Usuario usuario);
    List<PedidoResponseDTO> listarPendientesAsignables(Usuario usuario);
    // Consulta interna utilizada por RepartidorService tras validar el perfil propio.
    List<PedidoResponseDTO> listarPendientesAsignables();

    // Transiciones de estado
    PedidoResponseDTO marcarListoParaRetirar(Long id, Usuario usuario);
    PedidoResponseDTO asignarRepartidor(Long pedidoId, Long repartidorId);
    PedidoResponseDTO marcarRetirado(Long id);
    PedidoResponseDTO marcarRetiradoPorRepartidor(Long id, Long repartidorId);
    PedidoResponseDTO iniciarViaje(Long id);
    PedidoResponseDTO iniciarViajePorRepartidor(Long id, Long repartidorId);
    PedidoResponseDTO entregar(Long id);
    PedidoResponseDTO entregarPorRepartidor(Long id, Long repartidorId);
    PedidoResponseDTO cancelar(Long id, Usuario usuario);
}
