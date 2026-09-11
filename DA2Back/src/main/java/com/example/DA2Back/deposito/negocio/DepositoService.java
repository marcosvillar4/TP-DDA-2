package com.example.DA2Back.deposito.negocio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoMapper;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.excepcion.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class DepositoService implements IDeposito {

    private final DepositoRepository depositoRepository;
    private final ComercioRepository comercioRepository;

    @Override
    public DepositoResponseDTO obtenerPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del depósito no puede ser null");
        }

        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el depósito con ID: " + id));

        return DepositoMapper.toResponseDTO(deposito);
    }

    @Override
    public List<DepositoResponseDTO> obtenerTodos() {
        return depositoRepository.findAll().stream()
                .map(DepositoMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<DepositoResponseDTO> obtenerPorComercio(Long comercioId) {
        if (comercioId == null) {
            throw new IllegalArgumentException("El ID del comercio no puede ser null");
        }

        return depositoRepository.findByComercioId(comercioId).stream()
                .map(DepositoMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public DepositoResponseDTO crear(DepositoCreateDTO dto, Long usuarioId) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del depósito no pueden ser null");
        }
        if (usuarioId == null) {
            throw new IllegalArgumentException("El ID del usuario no puede ser null");
        }

        Deposito deposito = DepositoMapper.toEntity(dto, usuarioId);
        Deposito guardado = depositoRepository.save(deposito);

        return DepositoMapper.toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public DepositoResponseDTO actualizar(Long id, DepositoCreateDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del depósito no puede ser null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del depósito no pueden ser null");
        }

        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el depósito con ID: " + id));

        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());

        return DepositoMapper.toResponseDTO(depositoRepository.save(deposito));
    }

    @Override
    @Transactional
    public void asociarAComercio(Long depositoId, Long comercioId) {
        if (depositoId == null || comercioId == null) {
            throw new IllegalArgumentException("El ID del depósito y del comercio no pueden ser null");
        }

        Deposito deposito = depositoRepository.findById(depositoId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el depósito con ID: " + depositoId));

        if (deposito.getComercio() != null) {
            throw new AsociacionInvalidaException("El depósito ya está asociado a un comercio");
        }

        Comercio comercio = comercioRepository.findById(comercioId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el comercio con ID: " + comercioId));

        deposito.setComercio(comercio);
        depositoRepository.save(deposito);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del depósito no puede ser null");
        }

        Deposito deposito = depositoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el depósito con ID: " + id));

        depositoRepository.delete(deposito);
    }

    @Override
    public Deposito obtenerEntidadPorId(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("El ID del depósito no puede ser null");
        }

        return depositoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el depósito con ID: " + id));
    }
}

