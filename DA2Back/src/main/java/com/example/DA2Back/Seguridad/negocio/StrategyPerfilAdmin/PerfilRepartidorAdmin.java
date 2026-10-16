package com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin;

import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilRepartidorAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilRepartidorAdminDTO;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.dto.RepartidorUpdateDTO;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;
 
@Component
@RequiredArgsConstructor
public class PerfilRepartidorAdmin implements IPerfilAdmin {
 
    private final IRepartidorService repartidorService;
 
    @Override
    public Rol rol() {
        return Rol.REPARTIDOR;
    }
 
    @Override
    public Optional<PerfilAdminDTO> obtener(Usuario usuario) {
        try {
            RepartidorResponseDTO r = repartidorService.obtenerPorUsuarioId(usuario.getId());
            return Optional.of(PerfilRepartidorAdminDTO.builder()
                    .id(r.getId())
                    .vehiculo(usuario.getVehiculo())
                    .patente(r.getPatente())
                    .zona(r.getZona())
                    .estado(r.getEstado())
                    .activo(r.isActivo())
                    .build());
        } catch (NoSuchElementException e) {
            // RepartidorService lanza NoSuchElementException cuando el usuario aún no tiene perfil.
            return Optional.empty();
        }
    }
 
    @Override
    public void actualizar(Usuario usuario, ActualizarPerfilAdminDTO dto) {
        ActualizarPerfilRepartidorAdminDTO datos = validarTipo(dto);
        RepartidorResponseDTO actual = repartidorService.obtenerPorUsuarioId(usuario.getId());
 
        // RepartidorService.actualizar solo normaliza con trim(); las mayúsculas de la
        // patente se resuelven acá para no depender de cambios en ese módulo.
        RepartidorUpdateDTO cambios = new RepartidorUpdateDTO();
        cambios.setPatente(datos.getPatente().trim().toUpperCase());
        cambios.setZona(datos.getZona().trim());
        repartidorService.actualizar(actual.getId(), cambios);
 
        // El vehículo no pertenece a Repartidor sino a Usuario.
        usuario.setVehiculo(datos.getVehiculo());
    }
 
    private ActualizarPerfilRepartidorAdminDTO validarTipo(ActualizarPerfilAdminDTO dto) {
        if (dto instanceof ActualizarPerfilRepartidorAdminDTO repartidor) {
            return repartidor;
        }
        throw new IllegalArgumentException(
                "Los datos enviados no corresponden a un usuario con rol REPARTIDOR");
    }
}