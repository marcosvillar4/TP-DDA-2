package com.example.DA2Back.Seguridad.negocio;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;

import com.example.DA2Back.Seguridad.dato.UsuarioRepository;

/**
 * Implementacion de UserDetailsService que carga un usuario por su email.
 *
 * El campo "username" de Spring Security se mapea al email del usuario
 * ya que es el identificador unico utilizado en el login JWT.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado con email: " + email
                        )
                );
    }
}
