package com.example.DA2Back.Seguridad.negocio;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;

@Component
public class UsuarioFactory {
    public Usuario crearDesdeRegistro(RegisterDTO dto, PasswordEncoder passwordEncoder) {

        switch (dto.getRol()) {
            case ADMIN -> throw new IllegalArgumentException(
                    "El rol ADMINISTRADOR no puede crearse por auto-registro"
            );
            case COMERCIO, REPARTIDOR, DEPOSITO -> {
                // No se requiere acción adicional para estos roles, por ahora. Se podrían agregar validaciones específicas si es necesario.
            }
        }
        return Usuario.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .email(dto.getEmail())
                .rol(dto.getRol())
                .activo(true)
                .build();
    }

    //Creacion de un Administrador. No se expone via el endpoint publico de registro
    public Usuario crearAdministrador(String username, String rawPassword, String email,
                                       String nombre, PasswordEncoder passwordEncoder) {

        return Usuario.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .rol(Rol.ADMIN)
                .activo(true)
                .build();
    }
}
