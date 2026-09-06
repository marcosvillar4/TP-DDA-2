package com.example.DA2Back.deposito.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoMapper;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.negocio.IDeposito;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/deposito")
@RequiredArgsConstructor
public class DepositoController {

    private final IDeposito depositoService;

    @GetMapping
    public ResponseEntity<List<DepositoResponseDTO>> obtenerTodos() {

        List<DepositoResponseDTO> depositos =
                depositoService.obtenerTodos()
                        .stream()
                        .map(DepositoMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(depositos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepositoResponseDTO> obtenerPorId(
            @PathVariable Long id) {

        Deposito deposito = depositoService.obtenerPorId(id);

        return ResponseEntity.ok(
                DepositoMapper.toResponseDTO(deposito)
        );
    }

    @GetMapping("/comercio/{comercioId}")
    public ResponseEntity<List<DepositoResponseDTO>> obtenerPorComercio(
            @PathVariable Long comercioId) {

        List<DepositoResponseDTO> depositos =
                depositoService.obtenerPorComercio(comercioId)
                        .stream()
                        .map(DepositoMapper::toResponseDTO)
                        .collect(Collectors.toList());

        return ResponseEntity.ok(depositos);
    }

    @PostMapping
    public ResponseEntity<DepositoResponseDTO> crear(
            @RequestBody DepositoCreateDTO dto) {

        Deposito creado = depositoService.crear(
                dto.getNombre(),
                dto.getDireccion(),
                dto.getComercioId(),
                dto.getUsuarioId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(DepositoMapper.toResponseDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DepositoResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody DepositoCreateDTO dto) {

        Deposito actualizado = depositoService.actualizar(
                id,
                dto.getNombre(),
                dto.getDireccion(),
                dto.getComercioId(),
                dto.getUsuarioId()
        );

        return ResponseEntity.ok(
                DepositoMapper.toResponseDTO(actualizado)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        depositoService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}