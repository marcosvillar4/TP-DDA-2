package com.example.DA2Back.Seguridad.negocio;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;

import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.LoginDTO;
import com.example.DA2Back.Seguridad.dto.LoginResponseDTO;
import com.example.DA2Back.Seguridad.dto.RegisterDTO;
import com.example.DA2Back.Seguridad.dto.UsuarioResponseDTO;
import com.example.DA2Back.Seguridad.excepcion.CredencialesInvalidasException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final IUsuarioService usuarioService;
    private final ControlIntentosLoginService controlIntentosLoginService;

    @Override
    public UsuarioResponseDTO registrar(RegisterDTO dto) {
        return usuarioService.registrar(dto);
    }

    public LoginResponseDTO login(LoginDTO loginDTO) {

        String email = loginDTO.getEmail();

        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    loginDTO.getPassword()
                            )
                    );

            Usuario usuario = (Usuario) authentication.getPrincipal();

            // Login exitoso: se resetea el contador en memoria de intentos fallidos.
            controlIntentosLoginService.registrarLoginExitoso(email);

            String token = jwtService.generateToken(usuario);

            return new LoginResponseDTO(
                    token,
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getEmail(),
                    usuario.getRol()
            );

        } catch (BadCredentialsException ex) {
            // Contraseña incorrecta: cuenta como intento fallido.
            boolean recienBloqueado = controlIntentosLoginService.registrarIntentoFallido(email);
            if (recienBloqueado) {
                throw new CredencialesInvalidasException("La cuenta está bloqueada por intentos fallidos consecutivos");
            }
            throw new CredencialesInvalidasException("Email o contraseña incorrectos");

        } catch (LockedException ex) {
            // Cuenta ya bloqueada (estado BLOQUEADO): no suma intento nuevo.
            throw new CredencialesInvalidasException("La cuenta está bloqueada y no se puede iniciar sesión");

        } catch (DisabledException ex) {
            // Cuenta aun no validada (estado != VALIDADO): no suma intento nuevo.
            throw new CredencialesInvalidasException("La cuenta aún no fue validada");
        }
    }
}