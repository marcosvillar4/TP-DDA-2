package com.example.DA2Back.pedidos.presentacion;

import com.example.DA2Back.pedidos.dato.EstadoPedido;
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
 * Controlador REST de la capa de Presentación para Pedidos.
 *
 * Cada endpoint mapea directamente a una transición de estado del
 * patrón State, haciendo que la API sea autodescriptiva sobre qué
 * operaciones son posibles en el ciclo de vida de un pedido.
 */
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidosRestController {

    private final ServicioDePedidos servicioDePedidos;

    /** POST /api/pedidos — crea un nuevo pedido en estado CREADO */
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
    public ResponseEntity<List<PedidoResponseDTO>> listarPorComercio(@PathVariable Long comercioId) {
        return ResponseEntity.ok(servicioDePedidos.listarPorComercio(comercioId));
    }

    /** GET /api/pedidos/estado/{estado} — pedidos por estado */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<List<PedidoResponseDTO>> listarPorEstado(@PathVariable EstadoPedido estado) {
        return ResponseEntity.ok(servicioDePedidos.listarPorEstado(estado));
    }

    /** GET /api/pedidos/pendientes — pedidos CREADO sin repartidor */
    @GetMapping("/pendientes")
    public ResponseEntity<List<PedidoResponseDTO>> listarPendientesAsignables() {
        return ResponseEntity.ok(servicioDePedidos.listarPendientesAsignables());
    }

    /**
     * PATCH /api/pedidos/{id}/asignar/{repartidorId}
     * CREADO → ASIGNADO: asigna un repartidor al pedido.
     */
        @PatchMapping("/{id}/listo-para-retirar")
    public ResponseEntity<PedidoResponseDTO> marcarListoParaRetirar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDePedidos.marcarListoParaRetirar(id));
    }

    @PatchMapping("/{id}/asignar/{repartidorId}")
    public ResponseEntity<PedidoResponseDTO> asignarRepartidor(
            @PathVariable Long id,
            @PathVariable Long repartidorId) {
        return ResponseEntity.ok(servicioDePedidos.asignarRepartidor(id, repartidorId));
    }

    /**
     * PATCH /api/pedidos/{id}/iniciar-viaje
     * ASIGNADO → EN_CAMINO: el repartidor inicia el viaje.
     */
        @PatchMapping("/{id}/retirar")
    public ResponseEntity<PedidoResponseDTO> marcarRetirado(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDePedidos.marcarRetirado(id));
    }

    @PatchMapping("/{id}/iniciar-viaje")
    public ResponseEntity<PedidoResponseDTO> iniciarViaje(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDePedidos.iniciarViaje(id));
    }

    /**
     * PATCH /api/pedidos/{id}/entregar
     * EN_CAMINO → ENTREGADO: el repartidor confirma la entrega.
     */
    @PatchMapping("/{id}/entregar")
    public ResponseEntity<PedidoResponseDTO> entregar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDePedidos.entregar(id));
    }

    /**
     * DELETE /api/pedidos/{id}/cancelar
     * Cancela el pedido desde cualquier estado que lo permita.
     */
    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDePedidos.cancelar(id));
    }
}

