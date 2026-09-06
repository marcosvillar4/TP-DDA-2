package com.example.DA2Back.comercio.presentacion;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.ComercioService;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioMapper;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;

@RestController
@RequestMapping("/comercios")
public class ComercioController {

    private final IComercio comercioService;

    public ComercioController(ComercioService comercioService) {
        this.comercioService = comercioService;
    }

    @GetMapping
    public ResponseEntity<List<ComercioResponseDTO>> obtenerTodos() {

        List<ComercioResponseDTO> comercios = comercioService.obtenerTodos()
                .stream()
                .map(ComercioMapper::toResponseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(comercios);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComercioResponseDTO> obtenerPorId(
            @PathVariable Long id) {

        Comercio comercio = comercioService.obtenerPorId(id);

        return ResponseEntity.ok(
                ComercioMapper.toResponseDTO(comercio)
        );
    }

    @PostMapping
    public ResponseEntity<ComercioResponseDTO> crear(
            @RequestBody ComercioCreateDTO dto) {

        Comercio comercio = ComercioMapper.toEntity(dto);

        Comercio creado = comercioService.crear(comercio);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ComercioMapper.toResponseDTO(creado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComercioResponseDTO> actualizar(
            @PathVariable Long id,
            @RequestBody ComercioCreateDTO dto) {

        Comercio comercio = ComercioMapper.toEntity(dto);

        Comercio actualizado = comercioService.actualizar(id, comercio);

        return ResponseEntity.ok(
                ComercioMapper.toResponseDTO(actualizado)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        comercioService.eliminar(id);

        return ResponseEntity.noContent().build();
    }
}

