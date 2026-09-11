package com.example.DA2Back.deposito.negocio;

import java.util.List;

import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;

public interface IDeposito {

    DepositoResponseDTO obtenerPorId(Long id);
    List<DepositoResponseDTO> obtenerTodos();
    List<DepositoResponseDTO> obtenerPorComercio(Long comercioId);
    DepositoResponseDTO crear(DepositoCreateDTO dto, Long usuarioId);
    DepositoResponseDTO actualizar(Long id, DepositoCreateDTO dto);
    void asociarAComercio(Long depositoId, Long comercioId);
    void eliminar(Long id);
    Deposito obtenerEntidadPorId(Long id);

}

