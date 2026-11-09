package com.example.DA2Back.pago.presentacion;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.pago.dato.EstadoPago;
import com.example.DA2Back.pago.dto.PagarPedidoDTO;
import com.example.DA2Back.pago.dto.PagoResponseDTO;
import com.example.DA2Back.pago.negocio.IPagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Pagos del comercio autenticado. SecurityConfig restringe /api/pagos/** al rol
 * COMERCIO y el servicio valida ademas que cada pago pertenezca a ese comercio.
 */
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final IPagoService pagoService;

    /** GET /api/pagos/pendientes — pagos pendientes del comercio. */
    @GetMapping("/pendientes")
    public ResponseEntity<List<PagoResponseDTO>> listarPendientes(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pagoService.listarPendientesComercio(usuario));
    }

    /** GET /api/pagos?estado=PAGADO — historial del comercio, con filtro opcional. */
    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listar(
            @RequestParam(required = false) EstadoPago estado,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pagoService.listarComercio(usuario, estado));
    }

    /** GET /api/pagos/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtener(@PathVariable Long id,
                                                   @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pagoService.obtenerParaComercio(id, usuario));
    }

    /** POST /api/pagos/{id}/pagar — paga (simulado) el envio. */
    @PostMapping("/{id}/pagar")
    public ResponseEntity<PagoResponseDTO> pagar(@PathVariable Long id,
                                                 @Valid @RequestBody PagarPedidoDTO dto,
                                                 @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(pagoService.pagar(id, dto, usuario));
    }
}
