package com.example.DA2Back.repartidor.presentacion;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.repartidor.dto.CambiarEstadoRepartidorDTO;
import com.example.DA2Back.repartidor.dto.EntregaHistorialDTO;
import com.example.DA2Back.repartidor.dto.RepartidorCreateDTO;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.dto.RepartidorUpdateDTO;
import com.example.DA2Back.repartidor.dto.UsuarioRepartidorDisponibleDTO;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/repartidores")
@RequiredArgsConstructor
public class RepartidorController {

    private final IRepartidorService repartidorService;

    @GetMapping
    public ResponseEntity<List<RepartidorResponseDTO>> listar() {
        return ResponseEntity.ok(repartidorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepartidorResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(repartidorService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<RepartidorResponseDTO> crear(@RequestBody RepartidorCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repartidorService.crear(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RepartidorResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody RepartidorUpdateDTO dto) {
        return ResponseEntity.ok(repartidorService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<RepartidorResponseDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody CambiarEstadoRepartidorDTO dto) {
        return ResponseEntity.ok(repartidorService.cambiarEstado(id, dto));
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<RepartidorResponseDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(repartidorService.desactivar(id));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<RepartidorResponseDTO> activar(@PathVariable Long id) {
        return ResponseEntity.ok(repartidorService.activar(id));
    }

    @PostMapping("/{repartidorId}/pedidos/{pedidoId}/asignar")
    public ResponseEntity<RepartidorResponseDTO> asignarPedido(
            @PathVariable Long repartidorId,
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(repartidorService.asignarPedido(repartidorId, pedidoId));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<EntregaHistorialDTO>> obtenerHistorial(@PathVariable Long id) {
        return ResponseEntity.ok(repartidorService.obtenerHistorial(id));
    }

    @GetMapping("/pedidos/asignables")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPedidosAsignables() {
        return ResponseEntity.ok(repartidorService.obtenerPedidosAsignables());
    }

    @GetMapping("/usuarios-disponibles")
    public ResponseEntity<List<UsuarioRepartidorDisponibleDTO>> listarUsuariosDisponibles() {
        return ResponseEntity.ok(repartidorService.listarUsuariosRepartidoresDisponibles());
    }
}
