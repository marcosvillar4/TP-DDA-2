package com.example.DA2Back.service;

import com.example.DA2Back.dto.comercio.ComercioCreateDTO;
import com.example.DA2Back.dto.comercio.ComercioResponseDTO;

import java.util.List;

/**
 * Interfaz del componente de negocio ServicioDeComercios
 * segun el diagrama de arquitectura LogiRed.
 */
public interface ServicioDeComercios {

    ComercioResponseDTO crearComercio(ComercioCreateDTO dto);

    ComercioResponseDTO obtenerPorId(Long id);

    List<ComercioResponseDTO> listarTodos();

    void eliminarComercio(Long id);
}