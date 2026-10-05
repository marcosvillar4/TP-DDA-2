package com.example.DA2Back.comercio.presentacion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.comercio.negocio.IComercio;

import lombok.RequiredArgsConstructor;

import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;

@RestController
@RequestMapping("/comercios")
@RequiredArgsConstructor
public class ComercioController {

    private final IComercio comercioService;

    @GetMapping
    public ResponseEntity<List<ComercioResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(comercioService.obtenerTodos());
    }

    /** Comercio del usuario autenticado (rol COMERCIO). */
    @GetMapping("/me")
    public ResponseEntity<ComercioResponseDTO> obtenerMiComercio(
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(comercioService.obtenerPorUsuarioId(usuario.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComercioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comercioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMERCIO')")
    public ResponseEntity<ComercioResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ComercioCreateDTO dto,
            @AuthenticationPrincipal Usuario usuario) {

        if (usuario.getRol() != Rol.ADMIN && usuario.getRol() != Rol.COMERCIO) {
            throw new AccessDeniedException("No tenés permisos para modificar comercios");
        }

        // Un COMERCIO solo puede modificar su propio comercio.
        if (usuario.getRol() == Rol.COMERCIO) {
            Long duenoId = comercioService.obtenerPorId(id).getUsuarioId();
            if (!usuario.getId().equals(duenoId)) {
                throw new AccessDeniedException("No podés modificar un comercio que no es tuyo");
            }
        }

        return ResponseEntity.ok(comercioService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        comercioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
