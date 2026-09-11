package com.example.DA2Back.comercio.presentacion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{id}")
    public ResponseEntity<ComercioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comercioService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMERCIO')")
    public ResponseEntity<ComercioResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ComercioCreateDTO dto) {
        return ResponseEntity.ok(comercioService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        comercioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
