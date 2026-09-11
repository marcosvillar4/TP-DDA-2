package com.example.DA2Back.deposito.presentacion;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.dto.AsociarDepositoDTO;
import com.example.DA2Back.deposito.negocio.IDeposito;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/depositos")
@RequiredArgsConstructor
public class DepositoController {

    private final IDeposito depositoService;

    @GetMapping
    public ResponseEntity<List<DepositoResponseDTO>> obtenerTodos() {
        return ResponseEntity.ok(depositoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositoResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(depositoService.obtenerPorId(id));
    }

    @GetMapping("/comercio/{comercioId}")
    public ResponseEntity<List<DepositoResponseDTO>> obtenerPorComercio(@PathVariable Long comercioId) {
        return ResponseEntity.ok(depositoService.obtenerPorComercio(comercioId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEPOSITO')")
    public ResponseEntity<DepositoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody DepositoCreateDTO dto) {
        return ResponseEntity.ok(depositoService.actualizar(id, dto));
    }

    @PatchMapping("/asociar")
    @PreAuthorize("hasAnyRole('ADMIN', 'COMERCIO')")
    public ResponseEntity<Void> asociarAComercio(@Valid @RequestBody AsociarDepositoDTO dto) {
        depositoService.asociarAComercio(dto.getDepositoId(), dto.getComercioId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        depositoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}