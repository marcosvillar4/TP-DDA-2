package com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilComercioAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilComercioAdminDTO;
import com.example.DA2Back.Seguridad.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.negocio.IDeposito;
 
@Component
@RequiredArgsConstructor
public class PerfilComercioAdmin implements IPerfilAdmin {
 
    private final IComercio comercioService;
    private final IDeposito depositoService;
 
    @Override
    public Rol rol() {
        return Rol.COMERCIO;
    }
 
    @Override
    public Optional<PerfilAdminDTO> obtener(Usuario usuario) {
        try {
            ComercioResponseDTO c = comercioService.obtenerPorUsuarioId(usuario.getId());
 
            List<PerfilComercioAdminDTO.DepositoResumen> depositos =
                    depositoService.obtenerPorComercio(c.getId()).stream()
                            .map(d -> PerfilComercioAdminDTO.DepositoResumen.builder()
                                    .id(d.getId())
                                    .nombre(d.getNombre())
                                    .direccion(d.getDireccion())
                                    .usuarioId(d.getUsuarioId())
                                    .responsable(d.getResponsable())
                                    .usuarioEstado(d.getUsuarioEstado())
                                    .build())
                            .toList();
 
            return Optional.of(PerfilComercioAdminDTO.builder()
                    .id(c.getId())
                    .nombreComercial(c.getNombreComercial())
                    .razonSocial(c.getRazonSocial())
                    .cuit(c.getCuit())
                    .direccion(c.getDireccion())
                    .telefono(c.getTelefono())
                    .email(c.getEmail())
                    .maxDepositos(IDeposito.MAX_DEPOSITOS_POR_COMERCIO)
                    .depositos(depositos)
                    .build());
        } catch (RecursoNoEncontradoException e) {
            return Optional.empty();
        }
    }
 
    @Override
    public void actualizar(Usuario usuario, ActualizarPerfilAdminDTO dto) {
        ActualizarPerfilComercioAdminDTO datos = validarTipo(dto);
        ComercioResponseDTO actual = comercioService.obtenerPorUsuarioId(usuario.getId());
 
        // ComercioService.actualizar reemplaza todos los campos: se envían los seis.
        ComercioCreateDTO cambios = new ComercioCreateDTO();
        cambios.setNombreComercial(datos.getNombreComercial().trim());
        cambios.setRazonSocial(datos.getRazonSocial().trim());
        cambios.setDireccion(datos.getDireccion().trim());
        cambios.setCuit(datos.getCuit().trim());
        cambios.setTelefono(datos.getTelefono().trim());
        cambios.setEmail(datos.getEmail().trim());
 
        comercioService.actualizar(actual.getId(), cambios);
    }
 
    private ActualizarPerfilComercioAdminDTO validarTipo(ActualizarPerfilAdminDTO dto) {
        if (dto instanceof ActualizarPerfilComercioAdminDTO comercio) {
            return comercio;
        }
        throw new IllegalArgumentException(
                "Los datos enviados no corresponden a un usuario con rol COMERCIO");
    }
}
 