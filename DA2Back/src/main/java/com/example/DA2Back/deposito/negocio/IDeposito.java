package com.example.DA2Back.deposito.negocio;

import java.util.List;

import com.example.DA2Back.deposito.dato.Deposito;

public interface IDeposito {

    Deposito obtenerPorId(Long id);

    List<Deposito> obtenerTodos();

    List<Deposito> obtenerPorComercio(Long comercioId);

    Deposito crear(
            String nombre,
            String direccion,
            Long comercioId,
            Long usuarioId
    );

    Deposito actualizar(
            Long id,
            String nombre,
            String direccion,
            Long comercioId,
            Long usuarioId
    );

    void eliminar(Long id);
}

