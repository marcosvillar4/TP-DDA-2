package com.example.DA2Back.deposito.negocio;

import java.util.List;

import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;

public interface IDeposito {

    /** Máximo de depósitos que puede tener un comercio. */
    int MAX_DEPOSITOS_POR_COMERCIO = 10;

    DepositoResponseDTO obtenerPorId(Long id);
    /** Depósito operado por el usuario (rol DEPOSITO) indicado. */
    DepositoResponseDTO obtenerPorUsuarioId(Long usuarioId);
    /**
     * Lanza AsociacionInvalidaException si el comercio ya alcanzó el máximo
     * de depósitos ({@link #MAX_DEPOSITOS_POR_COMERCIO}).
     */
    void validarCupoDisponible(Long comercioId);
    List<DepositoResponseDTO> obtenerTodos();
    List<DepositoResponseDTO> obtenerPorComercio(Long comercioId);
    DepositoResponseDTO crear(DepositoCreateDTO dto, Long usuarioId);
    /** Crea el depósito ya vinculado al comercio indicado. */
    DepositoResponseDTO crear(DepositoCreateDTO dto, Long usuarioId, Long comercioId);
    DepositoResponseDTO actualizar(Long id, DepositoCreateDTO dto);
    void asociarAComercio(Long depositoId, Long comercioId);
    void eliminar(Long id);
    Deposito obtenerEntidadPorId(Long id);

}
