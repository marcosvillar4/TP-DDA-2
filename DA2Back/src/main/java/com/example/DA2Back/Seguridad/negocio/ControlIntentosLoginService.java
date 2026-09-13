package com.example.DA2Back.Seguridad.negocio;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.excepcion.TransicionInvalidaException;
import com.example.DA2Back.Seguridad.negocio.State.ResolverEstadoUsuario;

import lombok.RequiredArgsConstructor;

/**
 * Componente STATEFUL de la capa de Negocio de Seguridad.
 *
 * A diferencia de los demas @Service del sistema (stateless: no guardan
 * datos propios entre requests, solo consultan/actualizan la base), esta
 * clase mantiene en MEMORIA, mientras el proceso del backend esta vivo,
 * la cantidad de intentos de login fallidos por email.
 *
 * No persiste este contador en la base de datos: es un estado efimero,
 * propio de esta instancia del backend, que se pierde si la aplicacion
 * se reinicia. Al superar el umbral definido, delega el bloqueo real de
 * la cuenta en el patron State ya existente sobre Usuario
 * (IEstadoUsuario / ResolverEstadoUsuario), reutilizando esa logica de
 * negocio en lugar de duplicarla.
 */
@Component
@RequiredArgsConstructor
public class ControlIntentosLoginService {

    private static final Logger log = LoggerFactory.getLogger(ControlIntentosLoginService.class);

    private static final int MAX_INTENTOS_FALLIDOS = 5;

    /**
     * Estado en memoria del componente: email normalizado -> cantidad de
     * intentos fallidos consecutivos. Es la razon por la que este
     * componente es "stateful" (mantiene datos propios entre llamadas).
     */
    private final ConcurrentHashMap<String, AtomicInteger> intentosFallidosPorEmail = new ConcurrentHashMap<>();

    private final UsuarioRepository usuarioRepository;
    private final ResolverEstadoUsuario resolverEstadoUsuario;

    /**
     * Registra un intento de login fallido para el email dado.
     * Si se alcanza el umbral maximo, bloquea la cuenta automaticamente.
     */
    public boolean registrarIntentoFallido(String email) {
        String clave = normalizar(email);

        int intentos = intentosFallidosPorEmail
                .computeIfAbsent(clave, e -> new AtomicInteger(0))
                .incrementAndGet();

        log.info("Intento de login fallido para '{}' ({}/{})", clave, intentos, MAX_INTENTOS_FALLIDOS);

        if (intentos >= MAX_INTENTOS_FALLIDOS) {
            bloquearCuenta(clave);
            intentosFallidosPorEmail.remove(clave);
            return true; // Just blocked
        }
        return false;
    }

    /**
     * Resetea el contador de intentos fallidos tras un login exitoso.
     */
    public void registrarLoginExitoso(String email) {
        intentosFallidosPorEmail.remove(normalizar(email));
    }

    /**
     * Consulta utilitaria: cuantos intentos fallidos consecutivos
     * acumula actualmente el email dado (0 si no tiene ninguno).
     */
    public int obtenerIntentosFallidos(String email) {
        AtomicInteger contador = intentosFallidosPorEmail.get(normalizar(email));
        return contador != null ? contador.get() : 0;
    }

    private void bloquearCuenta(String email) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            try {
                resolverEstadoUsuario.resolver(usuario.getEstado()).bloquear(usuario);
                usuarioRepository.save(usuario);
                log.warn("Usuario '{}' bloqueado automaticamente tras {} intentos fallidos de login",
                        email, MAX_INTENTOS_FALLIDOS);
            } catch (TransicionInvalidaException ex) {
                // La cuenta ya esta bloqueada, rechazada o en un estado que no
                // admite bloqueo (ej. EN_EVALUACION): no hay nada mas que hacer.
                log.info("No se pudo bloquear automaticamente a '{}': {}", email, ex.getMessage());
            }
        });
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
