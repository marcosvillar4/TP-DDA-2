package com.example.DA2Back.comercio.negocio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.comercio.excepcion.*;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioMapper;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class ComercioService implements IComercio {

    private final ComercioRepository comercioRepository;

    @Override
    public ComercioResponseDTO obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }

        Comercio comercio = comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));

        return ComercioMapper.toResponseDTO(comercio);
    }

    @Override
    public List<ComercioResponseDTO> obtenerTodos() {
        return comercioRepository.findAll().stream()
                .map(ComercioMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ComercioResponseDTO crear(ComercioCreateDTO dto, Long usuarioId) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del comercio no pueden ser null");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }

        Comercio comercio = ComercioMapper.toEntity(dto, usuarioId);
        Comercio guardado = comercioRepository.save(comercio);

        return ComercioMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public ComercioResponseDTO actualizar(Long id, ComercioCreateDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del comercio no pueden ser null");
        }

        Comercio comercioExistente = comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));

        comercioExistente.setNombreComercial(dto.getNombreComercial());
        comercioExistente.setRazonSocial(dto.getRazonSocial());
        comercioExistente.setDireccion(dto.getDireccion());
        comercioExistente.setCUIT(dto.getCuit());
        comercioExistente.setTelefono(dto.getTelefono());
        comercioExistente.setEmail(dto.getEmail());

        return ComercioMapper.toResponseDTO(comercioRepository.save(comercioExistente));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }

        Comercio comercio = comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));

        comercioRepository.delete(comercio);
    }

    @Override
    public Comercio obtenerEntidadPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }
        return comercioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + id));
    }

    @Override
    public boolean existePorId(Long id) {
        return id != null && comercioRepository.existsById(id);
    }
}