package com.example.DA2Back.comercio.negocio;

import com.example.DA2Back.comercio.dto.ComercioCreateDTO;
import com.example.DA2Back.comercio.dto.ComercioResponseDTO;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.ComercioMapper;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.comercio.negocio.ServicioDeComercios;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Implementacion de ServicioDeComercios.
 */
@Service
@RequiredArgsConstructor
public class ServicioDeComerciosImpl implements ServicioDeComercios {

    private final ComercioRepository comercioRepository;

    @Override
    @Transactional
    public ComercioResponseDTO crearComercio(ComercioCreateDTO dto) {
        if (comercioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un comercio registrado con el email: " + dto.getEmail());
        }

        Comercio comercio = ComercioMapper.toEntity(dto);
        Comercio guardado = comercioRepository.save(comercio);
        return ComercioMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ComercioResponseDTO obtenerPorId(Long id) {
        Comercio comercio = comercioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Comercio con id=" + id + " no encontrado"));
        return ComercioMapper.toResponseDTO(comercio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComercioResponseDTO> listarTodos() {
        return comercioRepository.findAll()
                .stream()
                .map(ComercioMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarComercio(Long id) {
        if (!comercioRepository.existsById(id)) {
            throw new NoSuchElementException("Comercio con id=" + id + " no encontrado");
        }
        comercioRepository.deleteById(id);
    }
}