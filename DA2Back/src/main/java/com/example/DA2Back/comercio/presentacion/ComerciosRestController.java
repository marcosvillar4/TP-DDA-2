package com.example.DA2Back.comercio.presentacion;

import com.example.DA2Back.comercio.dto.ComercioCreateDTO;
import com.example.DA2Back.comercio.dto.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.ServicioDeComercios;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para el recurso Comercio (ComerciosView).
 */
@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class ComerciosRestController {

    private final ServicioDeComercios servicioDeComercios;

    @PostMapping
    public ResponseEntity<ComercioResponseDTO> crearComercio(@RequestBody ComercioCreateDTO dto) {
        ComercioResponseDTO respuesta = servicioDeComercios.crearComercio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @GetMapping
    public ResponseEntity<List<ComercioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(servicioDeComercios.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComercioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDeComercios.obtenerPorId(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarComercio(@PathVariable Long id) {
        servicioDeComercios.eliminarComercio(id);
        return ResponseEntity.noContent().build();
    }
}