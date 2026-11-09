package com.example.DA2Back.pago.presentacion;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.pago.dato.EstadoPago;
import com.example.DA2Back.pago.dto.PagoResponseDTO;
import com.example.DA2Back.pago.negocio.IPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Cobros del repartidor autenticado. /repartidor/** ya esta restringido al rol
 * REPARTIDOR en SecurityConfig.
 */
@RestController
@RequestMapping("/repartidor/me/pagos")
@RequiredArgsConstructor
public class PagoRepartidorController {

    private final IPagoService pagoService;

    /**
     * GET /repartidor/me/pagos[?estado=PENDIENTE|PAGADO] — cobros recibidos y pendientes,
     * cada uno con el pedido al que corresponde.
     */
    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listarMisCobros(
            @RequestParam(required = false) EstadoPago estado,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pagoService.listarParaRepartidor(usuario, estado));
    }
}
