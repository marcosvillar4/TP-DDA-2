package com.example.DA2Back.inventario.negocio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.InventarioRepository;

@Service
public class InventarioService implements IInventario {

    private final InventarioRepository inventarioRepository;
    private final IComercio comercioService;

    public InventarioService(
            InventarioRepository inventarioRepository,
            IComercio comercioService) {

        this.inventarioRepository = inventarioRepository;
        this.comercioService = comercioService;
    }

    @Override
    public Inventario obtenerPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del inventario no puede ser nulo"
            );
        }

        return inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el inventario con ID: " + id
                ));
    }

    @Override
    public Inventario obtenerPorComercio(Long comercioId) {

        if (comercioId == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio no puede ser nulo"
            );
        }

        return inventarioRepository.findByComercioId(comercioId)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró un inventario para el comercio con ID: "
                                + comercioId
                ));
    }

    @Override
    public List<Inventario> obtenerTodos() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario crear(Long comercioId) {

        if (comercioId == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio no puede ser nulo"
            );
        }

        if (inventarioRepository.findByComercioId(comercioId).isPresent()) {
            throw new RuntimeException(
                    "El comercio ya posee un inventario"
            );
        }

        Comercio comercio = comercioService.obtenerEntidadPorId(comercioId);

        Inventario inventario = new Inventario();
        inventario.setComercio(comercio);

        return inventarioRepository.save(inventario);
    }

    @Override
    public void eliminar(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del inventario no puede ser nulo"
            );
        }

        Inventario inventario = obtenerPorId(id);

        inventarioRepository.delete(inventario);
    }
}

