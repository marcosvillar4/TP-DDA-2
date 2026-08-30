package com.example.DA2Back.comercio.negocio;

import com.example.DA2Back.comercio.dto.ComercioCreateDTO;
import com.example.DA2Back.comercio.dto.ComercioResponseDTO;

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