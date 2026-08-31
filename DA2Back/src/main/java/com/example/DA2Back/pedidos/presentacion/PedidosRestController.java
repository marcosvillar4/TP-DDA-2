package com.example.DA2Back.pedidos.presentacion;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dto.ActualizarEstadoDTO;
import com.example.DA2Back.pedidos.dto.CrearPedidoDTO;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.pedidos.negocio.ServicioDePedidos;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST de la capa de Presentacion (PedidosView).
 * Mapeado a /api/pedidos. Inyecta la interfaz ServicioDePedidos (IoC).
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidosRestController {

    private final ServicioDePedidos servicioDePedidos;

    /** POST /api/pedidos — crea un nuevo pedido */
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@RequestBody CrearPedidoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioDePedidos.crearPedido(dto));
    }

    /** GET /api/pedidos — lista todos los pedidos */
    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(servicioDePedidos.listarTodos());
    }

    /** GET /api/pedidos/{id} — obtiene un pedido por ID */
    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDePedidos.obtenerPorId(id));
    }

    /** GET /api/pedidos/comercio/{comercioId} — pedidos de un comercio */
    @GetMapping("/comercio/{comercioId}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorComercio(
            @PathVariable Long comercioId) {
        return ResponseEntity.ok(servicioDePedidos.listarPorComercio(comercioId));
    }

    /** GET /api/pedidos/estado/{estado} — pedidos por estado */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorEstado(
            @PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(servicioDePedidos.listarPorEstado(estado));
    }

    /** PATCH /api/pedidos/{id}/estado — actualiza el estado de un pedido */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoDTO dto) {
        return ResponseEntity.ok(servicioDePedidos.actualizarEstado(id, dto));
    }

    /** DELETE /api/pedidos/{id}/cancelar — cancela un pedido */
    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDePedidos.cancelar(id));
    }
}