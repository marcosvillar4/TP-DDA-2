package com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilDepositoAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilDepositoAdminDTO;
import com.example.DA2Back.Seguridad.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.negocio.IDeposito;
 
@Component
@RequiredArgsConstructor
public class PerfilDepositoAdmin implements IPerfilAdmin {
 
    private final IDeposito depositoService;
    private final IComercio comercioService;
 
    @Override
    public Rol rol() {
        return Rol.DEPOSITO;
    }
 
    @Override
    public Optional<PerfilAdminDTO> obtener(Usuario usuario) {
        try {
            DepositoResponseDTO d = depositoService.obtenerPorUsuarioId(usuario.getId());
 
            PerfilDepositoAdminDTO.ComercioAsociado comercio = null;
            if (d.getComercioId() != null) {
                // Se resuelve el comercio completo para conocer el usuarioId de su dueño
                // (DepositoResponseDTO solo trae id y nombre) y poder navegar a su detalle.
                ComercioResponseDTO c = comercioService.obtenerPorId(d.getComercioId());
                comercio = PerfilDepositoAdminDTO.ComercioAsociado.builder()
                        .id(c.getId())
                        .nombreComercial(c.getNombreComercial())
                        .usuarioId(c.getUsuarioId())
                        .build();
            }
 
            return Optional.of(PerfilDepositoAdminDTO.builder()
                    .id(d.getId())
                    .nombre(d.getNombre())
                    .direccion(d.getDireccion())
                    .comercio(comercio)
                    .build());
        } catch (RecursoNoEncontradoException e) {
            return Optional.empty();
        }
    }
 
    @Override
    public void actualizar(Usuario usuario, ActualizarPerfilAdminDTO dto) {
        ActualizarPerfilDepositoAdminDTO datos = validarTipo(dto);
        DepositoResponseDTO actual = depositoService.obtenerPorUsuarioId(usuario.getId());
 
        DepositoCreateDTO cambios = new DepositoCreateDTO();
        cambios.setNombre(datos.getNombre().trim());
        cambios.setDireccion(datos.getDireccion().trim());
        depositoService.actualizar(actual.getId(), cambios);
    }
 
    private ActualizarPerfilDepositoAdminDTO validarTipo(ActualizarPerfilAdminDTO dto) {
        if (dto instanceof ActualizarPerfilDepositoAdminDTO deposito) {
            return deposito;
        }
        throw new IllegalArgumentException(
                "Los datos enviados no corresponden a un usuario con rol DEPOSITO");
    }
}
