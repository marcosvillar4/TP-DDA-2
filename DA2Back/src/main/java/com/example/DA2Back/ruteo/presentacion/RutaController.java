package com.example.DA2Back.ruteo.presentacion;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;
import com.example.DA2Back.ruteo.dto.RutaDTO;
import com.example.DA2Back.ruteo.negocio.ServicioDeRutas;

import lombok.RequiredArgsConstructor;

/**
 * Endpoints del sistema de ruteo. SecurityConfig restringe:
 *  - /repartidor/**            -> rol REPARTIDOR
 *  - /api/pedidos/{id}/ruta    -> ADMIN, COMERCIO, DEPOSITO (dentro de su alcance)
 */
@RestController
@RequiredArgsConstructor
public class RutaController {

    private final ServicioDeRutas servicioDeRutas;
    private final IRepartidorService repartidorService;

    /** Recorrido pendiente del repartidor autenticado, ordenado. lat/lng opcionales (su ubicacion). */
    @GetMapping("/repartidor/me/ruta")
    public ResponseEntity<RutaDTO> miRuta(@AuthenticationPrincipal Usuario usuario,
                                          @RequestParam(required = false) Double lat,
                                          @RequestParam(required = false) Double lng) {
        Long repartidorId = repartidorService.obtenerPorUsuarioId(usuario.getId()).getId();
        return ResponseEntity.ok(servicioDeRutas.rutaDeRepartidor(repartidorId, lat, lng));
    }

    /** Recorrido retiro -> entrega de un pedido. */
    @GetMapping("/api/pedidos/{id}/ruta")
    public ResponseEntity<RutaDTO> rutaDePedido(@PathVariable Long id,
                                                @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(servicioDeRutas.rutaDePedido(id, usuario));
    }
}
