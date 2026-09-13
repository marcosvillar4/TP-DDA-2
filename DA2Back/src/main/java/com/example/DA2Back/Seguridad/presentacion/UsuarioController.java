package com.example.DA2Back.Seguridad.presentacion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DA2Back.Seguridad.dto.ActualizarUsuarioAdminDTO;
import com.example.DA2Back.Seguridad.dto.CambiarPasswordAdminDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.example.DA2Back.Seguridad.negocio.IUsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    /** Devuelve el perfil del usuario autenticado (según el JWT recibido). */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> obtenerMiPerfil(Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(authentication.getName()));
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @PatchMapping("/admin/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuarioAdmin(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioAdminDTO dto) {
        return ResponseEntity.ok(usuarioService.actualizarUsuarioAdmin(id, dto));
    }

    @PatchMapping("/admin/{id}/password")
    public ResponseEntity<Void> resetearPassword(
            @PathVariable Long id,
            @Valid @RequestBody CambiarPasswordAdminDTO dto) {
        usuarioService.resetearPasswordAdmin(id, dto);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/{id}/validar")
    public ResponseEntity<UsuarioResponseDTO> validar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.validar(id));
    }

    @PatchMapping("/admin/{id}/rechazar")
    public ResponseEntity<UsuarioResponseDTO> rechazar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.rechazar(id));
    }

    @PatchMapping("/admin/{id}/bloquear")
    public ResponseEntity<UsuarioResponseDTO> bloquear(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.bloquear(id));
    }

    @PatchMapping("/admin/{id}/desbloquear")
    public ResponseEntity<UsuarioResponseDTO> desbloquear(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.desbloquear(id));
    }
}