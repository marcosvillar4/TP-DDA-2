package com.example.DA2Back.comercio.negocio;

import java.util.List;

import com.example.DA2Back.comercio.dato.Comercio;

public interface IComercio {

    Comercio obtenerPorId(Long id);

    List<Comercio> obtenerTodos();

    Comercio crear(Comercio comercio);

    Comercio actualizar(Long id, Comercio comercio);

    void eliminar(Long id);
}