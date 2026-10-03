package com.example.DA2Back.Seguridad.negocio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.RegistroComercioDTO;
import com.example.DA2Back.Seguridad.dto.RegistroDepositoDTO;
import com.example.DA2Back.Seguridad.dto.RegistroRepartidorDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioFactory {

    private final PasswordEncoder passwordEncoder;
    private final IComercio comercioService;

    public Usuario crearDesdeRegistro(RegisterDTO dto) {
        if (dto.getRol() == Rol.ADMIN) {
            throw new IllegalArgumentException("El rol ADMIN no puede crearse por auto-registro");
        }
        if (dto.getRol() == Rol.DEPOSITO) {
            throw new IllegalArgumentException(
                    "Los depósitos no se auto-registran: los da de alta un comercio desde su panel");
        }

        Usuario.UsuarioBuilder builder = construirUsuarioBase(dto, dto.getRol());

        if (dto instanceof RegistroRepartidorDTO repartidorDTO) {
            builder.vehiculo(repartidorDTO.getVehiculo());
        }

        return builder.build();
    }

    /**
     * Crea el usuario (rol DEPOSITO) que opera un depósito dado de alta por un
     * comercio. Reutiliza la misma construcción base que el registro: queda
     * EN_EVALUACION hasta que un administrador lo valide.
     */
    public Usuario crearUsuarioDeposito(RegistroDepositoDTO dto) {
        return construirUsuarioBase(dto, Rol.DEPOSITO).build();
    }

    public Usuario crearAdministrador(String email, String rawPassword, String nombre,
                                       String apellido, String dni, String telefono) {
        return Usuario.builder()
                .email(email)
                .password(passwordEncoder.encode(rawPassword))
                .nombre(nombre)
                .apellido(apellido)
                .DNI(dni)
                .telefono(telefono)
                .rol(Rol.ADMIN)
                .estado(EstadoUsuario.VALIDADO)
                .build();
    }

    /**
     * Crea la entidad de dominio asociada al usuario recién registrado
     * (Comercio). REPARTIDOR no necesita una entidad aparte: su único dato
     * extra (vehiculo) ya se guardó directo en el Usuario. Los depósitos no
     * pasan por acá: los crea un comercio (ver UsuarioServiceImpl).
     */
    public void crearEntidadRelacionada(RegisterDTO dto, Long usuarioId) {
        if (dto instanceof RegistroComercioDTO comercioDTO) {
            comercioService.crear(mapearComercio(comercioDTO), usuarioId);
        }
    }

    private Usuario.UsuarioBuilder construirUsuarioBase(RegisterDTO dto, Rol rol) {
        return Usuario.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .DNI(dto.getDni())
                .telefono(dto.getTelefono())
                .rol(rol)
                .estado(EstadoUsuario.EN_EVALUACION);
    }

    private ComercioCreateDTO mapearComercio(RegistroComercioDTO dto) {
        ComercioCreateDTO d = new ComercioCreateDTO();
        d.setNombreComercial(dto.getNombreComercial());
        d.setRazonSocial(dto.getRazonSocial());
        d.setDireccion(dto.getDireccion());
        d.setCuit(dto.getCuit());
        d.setTelefono(dto.getTelefono());
        d.setEmail(dto.getEmail());
        return d;
    }
}
