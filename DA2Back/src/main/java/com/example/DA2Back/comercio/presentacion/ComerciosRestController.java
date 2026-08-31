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
 * Controlador REST de la capa de Presentacion para el recurso Comercio.
 * Mapeado a /api/comercios. Inyecta la interfaz ServicioDeComercios (IoC).
 */
@RestController
@RequestMapping("/api/comercios")
@RequiredArgsConstructor
public class ComerciosRestController {

    private final ServicioDeComercios servicioDeComercios;

    /** POST /api/comercios — registra un nuevo comercio */
    @PostMapping
    public ResponseEntity<ComercioResponseDTO> registrar(@RequestBody ComercioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(servicioDeComercios.registrar(dto));
    }

    /** GET /api/comercios — lista todos los comercios */
    @GetMapping
    public ResponseEntity<List<ComercioResponseDTO>> listar() {
        return ResponseEntity.ok(servicioDeComercios.listar());
    }

    /** GET /api/comercios/{id} — obtiene un comercio por ID */
    @GetMapping("/{id}")
    public ResponseEntity<ComercioResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicioDeComercios.obtenerPorId(id));
    }

    /** DELETE /api/comercios/{id} — elimina un comercio */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioDeComercios.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}