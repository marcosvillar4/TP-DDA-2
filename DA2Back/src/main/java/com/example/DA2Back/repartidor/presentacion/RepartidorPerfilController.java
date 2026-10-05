package com.example.DA2Back.repartidor.presentacion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DA2Back.Seguridad.dato.Usuario;
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
}
