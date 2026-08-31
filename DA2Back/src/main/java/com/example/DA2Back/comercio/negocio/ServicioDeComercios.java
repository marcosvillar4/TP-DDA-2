package com.example.DA2Back.comercio.negocio;

import com.example.DA2Back.comercio.dto.ComercioCreateDTO;
import com.example.DA2Back.comercio.dto.ComercioResponseDTO;

import java.util.List;

/**
 * Contrato de negocio del componente ServicioDeComercios,
 * segun el diagrama de arquitectura LogiRed.
 *
 * Los controladores dependen de esta interfaz (Inversion de Control).
 */
public interface ServicioDeComercios {

    /** Registra un nuevo comercio en el sistema. */
    ComercioResponseDTO registrar(ComercioCreateDTO dto);

    /** Retorna un comercio por su identificador unico. */
    ComercioResponseDTO obtenerPorId(Long id);

    /** Retorna un comercio por su email (util para el DataInitializer y lookups internos). */
    ComercioResponseDTO obtenerPorEmail(String email);

    /** Retorna todos los comercios registrados. */
    List<ComercioResponseDTO> listar();

    /** Elimina un comercio por su identificador. */
    void eliminar(Long id);
}