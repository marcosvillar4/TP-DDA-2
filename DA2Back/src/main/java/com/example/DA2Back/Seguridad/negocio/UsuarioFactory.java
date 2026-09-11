package com.example.DA2Back.Seguridad.negocio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.RegistroComercioDTO;
import com.example.DA2Back.Seguridad.dto.RegistroDepositoDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioFactory {

    private final PasswordEncoder passwordEncoder;
    private final IComercio comercioService;
    private final IDeposito depositoService;

    public Usuario crearDesdeRegistro(RegisterDTO dto) {
        if (dto.getRol() == Rol.ADMIN) {
            throw new IllegalArgumentException("El rol ADMIN no puede crearse por auto-registro");
        }

        return Usuario.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nombre(dto.getNombre())
                .apellido(dto.getApellido())
                .DNI(dto.getDni())
                .telefono(dto.getTelefono())
                .rol(dto.getRol())
                .estado(EstadoUsuario.EN_EVALUACION)
                .build();
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

    public void crearEntidadRelacionada(RegisterDTO dto, Long usuarioId) {
        if (dto instanceof RegistroComercioDTO comercioDTO) {
            comercioService.crear(mapearComercio(comercioDTO), usuarioId);

        } else if (dto instanceof RegistroDepositoDTO depositoDTO) {
            depositoService.crear(mapearDeposito(depositoDTO), usuarioId);
        }
        // REPARTIDOR: pendiente
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

    private DepositoCreateDTO mapearDeposito(RegistroDepositoDTO dto) {
        DepositoCreateDTO d = new DepositoCreateDTO();
        d.setNombre(dto.getNombreDeposito());
        d.setDireccion(dto.getDireccionDeposito());
        return d;
    }
}
