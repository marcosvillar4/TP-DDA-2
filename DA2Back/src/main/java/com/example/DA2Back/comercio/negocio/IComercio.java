package com.example.DA2Back.comercio.negocio;

import java.util.List;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;

public interface IComercio {

    ComercioResponseDTO obtenerPorId(Long id);
    List<ComercioResponseDTO> obtenerTodos();
    ComercioResponseDTO crear(ComercioCreateDTO dto, Long usuarioId);
    ComercioResponseDTO actualizar(Long id, ComercioCreateDTO dto);
    void eliminar(Long id);
    Comercio obtenerEntidadPorId(Long id);
    boolean existePorId(Long id);
}