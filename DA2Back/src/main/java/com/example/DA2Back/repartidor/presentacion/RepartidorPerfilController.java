package com.example.DA2Back.repartidor.presentacion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.pedidos.dto.PedidoResponseDTO;
import com.example.DA2Back.repartidor.dto.EntregaHistorialDTO;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints del propio repartidor autenticado (rol REPARTIDOR).
 * SecurityConfig restringe /repartidor/** a ese rol; la administración de la
 * flota sigue en /repartidores/** (solo ADMIN).
 */
@RestController
@RequestMapping("/repartidor")
@RequiredArgsConstructor
public class RepartidorPerfilController {

    private final IRepartidorService repartidorService;

    /** Perfil del repartidor autenticado, con su pedido actual (si tiene). */
    @GetMapping("/me")
    public ResponseEntity<RepartidorResponseDTO> obtenerMiPerfil(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(repartidorService.obtenerPorUsuarioId(usuario.getId()));
    }

    /** Entregas finalizadas (entregadas o canceladas) del repartidor autenticado. */
    @GetMapping("/me/historial")
    public ResponseEntity<List<EntregaHistorialDTO>> obtenerMiHistorial(
            @AuthenticationPrincipal Usuario usuario) {
        Long repartidorId = repartidorService.obtenerPorUsuarioId(usuario.getId()).getId();
        return ResponseEntity.ok(repartidorService.obtenerHistorial(repartidorId));
    }

    /** Pedidos listos para retirar que el repartidor autenticado puede consultar. */
    @GetMapping("/me/pedidos-disponibles")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerMisPedidosDisponibles(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(repartidorService.obtenerPedidosDisponiblesParaUsuario(usuario.getId()));
    }

    /** Toma un pedido listo para retirar para el repartidor autenticado. */
    @PatchMapping("/me/pedidos/{pedidoId}/tomar")
    public ResponseEntity<PedidoResponseDTO> tomarPedido(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(repartidorService.tomarPedidoParaUsuario(usuario.getId(), pedidoId));
    }

    /** Confirma que el repartidor autenticado retiró el pedido asignado. */
    @PatchMapping("/me/pedidos/{pedidoId}/retirar")
    public ResponseEntity<PedidoResponseDTO> retirarPedido(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(repartidorService.retirarPedidoParaUsuario(usuario.getId(), pedidoId));
    }

    /** Inicia el viaje del pedido retirado por el repartidor autenticado. */
    @PatchMapping("/me/pedidos/{pedidoId}/iniciar-viaje")
    public ResponseEntity<PedidoResponseDTO> iniciarViaje(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(repartidorService.iniciarViajeParaUsuario(usuario.getId(), pedidoId));
    }

    /** Marca como entregado el pedido propio en camino. */
    @PatchMapping("/me/pedidos/{pedidoId}/entregar")
    public ResponseEntity<PedidoResponseDTO> entregarPedido(
            @AuthenticationPrincipal Usuario usuario,
            @PathVariable Long pedidoId) {
        return ResponseEntity.ok(repartidorService.entregarPedidoParaUsuario(usuario.getId(), pedidoId));
    }
}
