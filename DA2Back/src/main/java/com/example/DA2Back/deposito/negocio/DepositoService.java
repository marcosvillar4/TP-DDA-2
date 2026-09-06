package com.example.DA2Back.deposito.negocio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;

import lombok.RequiredArgsConstructor;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;

@Service
@RequiredArgsConstructor 
public class DepositoService implements IDeposito {

    private final DepositoRepository depositoRepository;
    private final IComercio comercioService;
    private final UsuarioRepository usuarioRepository;

    @Override
    public Deposito obtenerPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del depósito no puede ser null"
            );
        }

        return depositoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "No se encontró el depósito con ID: " + id
                ));
    }

    @Override
    public List<Deposito> obtenerTodos() {
        return depositoRepository.findAll();
    }

    @Override
    public List<Deposito> obtenerPorComercio(Long comercioId) {

        if (comercioId == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio no puede ser null"
            );
        }

        return depositoRepository.findByComercioId(comercioId);
    }

    @Override
    public Deposito crear(
            String nombre,
            String direccion,
            Long comercioId,
            Long usuarioId) {

        if (nombre == null || direccion == null) {
            throw new IllegalArgumentException(
                    "El nombre y la dirección son obligatorios"
            );
        }

        if (comercioId == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio no puede ser null"
            );
        }

        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario no puede ser null"
            );
        }

        Comercio comercio = comercioService.obtenerPorId(comercioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException(
                "No se encontró el usuario con ID: " + usuarioId
        ));

        Deposito deposito = new Deposito();

        deposito.setNombre(nombre);
        deposito.setDireccion(direccion);
        deposito.setComercio(comercio);
        deposito.setUsuario(usuario);

        return depositoRepository.save(deposito);
    }

    @Override
    public Deposito actualizar(
            Long id,
            String nombre,
            String direccion,
            Long comercioId,
            Long usuarioId) {

        Deposito deposito = obtenerPorId(id);

        if (nombre == null || direccion == null) {
            throw new IllegalArgumentException(
                    "El nombre y la dirección son obligatorios"
            );
        }

        if (comercioId == null) {
            throw new IllegalArgumentException(
                    "El ID del comercio no puede ser null"
            );
        }

        if (usuarioId == null) {
            throw new IllegalArgumentException(
                    "El ID del usuario no puede ser null"
            );
        }

        Comercio comercio = comercioService.obtenerPorId(comercioId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException(
                "No se encontró el usuario con ID: " + usuarioId
        ));

        deposito.setNombre(nombre);
        deposito.setDireccion(direccion);
        deposito.setComercio(comercio);
        deposito.setUsuario(usuario);

        return depositoRepository.save(deposito);
    }

    @Override
    public void eliminar(Long id) {

        Deposito deposito = obtenerPorId(id);

        depositoRepository.delete(deposito);
    }
}

