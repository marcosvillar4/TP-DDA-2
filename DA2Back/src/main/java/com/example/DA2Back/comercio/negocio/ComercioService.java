package com.example.DA2Back.comercio.negocio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ComercioService implements IComercio {

    private final ComercioRepository comercioRepository;

    @Override
    public Comercio obtenerPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }

        return comercioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el comercio con ID: " + id
                ));
    }

    @Override
    public List<Comercio> obtenerTodos() {
        return comercioRepository.findAll();
    }

    @Override
    public Comercio crear(Comercio comercio) {

        if (comercio == null) {
            throw new IllegalArgumentException(
                    "El comercio no puede ser null"
            );
        }

        return comercioRepository.save(comercio);
    }

    @Override
    public Comercio actualizar(Long id, Comercio comercio) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio no puede ser null"
            );
        }

        if (comercio == null) {
            throw new IllegalArgumentException(
                    "El comercio no puede ser null"
            );
        }

        Comercio comercioExistente = obtenerPorId(id);

        comercioExistente.setNombre(comercio.getNombre());
        comercioExistente.setDireccion(comercio.getDireccion());
        comercioExistente.setTelefono(comercio.getTelefono());
        comercioExistente.setEmail(comercio.getEmail());

        return comercioRepository.save(comercioExistente);
    }

    @Override
    public void eliminar(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio no puede ser null"
            );
        }

        Comercio comercio = obtenerPorId(id);

        comercioRepository.delete(comercio);
    }
}