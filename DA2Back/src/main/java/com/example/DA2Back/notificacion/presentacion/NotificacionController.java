package com.example.DA2Back.notificacion.presentacion;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.notificacion.dto.NotificacionResponseDTO;
import com.example.DA2Back.notificacion.negocio.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @GetMapping
    public ResponseEntity<List<NotificacionResponseDTO>> listar(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacionService.listarApp(usuario));
    }

    @GetMapping("/no-leidas")
    public ResponseEntity<Map<String, Long>> contarNoLeidas(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(Map.of("cantidad", notificacionService.contarNoLeidas(usuario)));
    }

    @PatchMapping("/{id}/leida")
    public ResponseEntity<NotificacionResponseDTO> marcarLeida(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(notificacionService.marcarLeida(id, usuario));
    }
}
