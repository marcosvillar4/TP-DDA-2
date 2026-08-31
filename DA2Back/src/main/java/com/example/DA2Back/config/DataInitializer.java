package com.example.DA2Back.config;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.pedidos.dato.EstadoPedido;
import com.example.DA2Back.pedidos.dato.Pedido;
import com.example.DA2Back.pedidos.dato.PedidosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Componente de semilla de datos inicial.
 *
 * Se ejecuta automaticamente al arrancar la aplicacion y siembra
 * datos basicos solo si la base de datos esta vacia. De esta forma
 * es idempotente: no duplica registros en reinicios.
 *
 * Datos sembrados:
 *   - 1 Usuario Administrador (admin@logired.com)
 *   - 1 Usuario con rol COMERCIO (comercio@logired.com) + entidad Comercio
 *   - 1 Pedido inicial en estado CREADO asociado al comercio
 *
 * Nota: este componente NO se activa en el perfil "test" para no
 * interferir con la base de datos H2 de las pruebas unitarias.
 */
@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final ComercioRepository comercioRepository;
    private final PedidosRepository pedidosRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        // ----------------------------------------------------------------
        // 1. Usuario Administrador
        // ----------------------------------------------------------------
        if (!usuarioRepository.existsByEmail("admin@logired.com")) {
            Usuario admin = Usuario.builder()
                    .username("Administrador LogiRed")
                    .email("admin@logired.com")
                    .password(passwordEncoder.encode("Admin1234!"))
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info("[DataInitializer] Usuario ADMIN creado: admin@logired.com");
        }

        // ----------------------------------------------------------------
        // 2. Usuario Comercio + entidad Comercio
        // ----------------------------------------------------------------
        Comercio comercio;
        if (!comercioRepository.existsByEmail("comercio@logired.com")) {
            comercio = Comercio.builder()
                    .nombre("Pizzeria Central LogiRed")
                    .direccion("Av. Corrientes 1234, CABA")
                    .telefono("1122334455")
                    .email("comercio@logired.com")
                    .build();
            comercio = comercioRepository.save(comercio);
            log.info("[DataInitializer] Comercio creado: {} (id={})",
                    comercio.getNombre(), comercio.getId());
        } else {
            comercio = comercioRepository.findByEmail("comercio@logired.com")
                    .orElseThrow();
        }

        if (!usuarioRepository.existsByEmail("comercio@logired.com")) {
            Usuario usuarioComercio = Usuario.builder()
                    .username("Comercio Central")
                    .email("comercio@logired.com")
                    .password(passwordEncoder.encode("Comercio1234!"))
                    .rol(Rol.COMERCIO)
                    .activo(true)
                    .build();
            usuarioRepository.save(usuarioComercio);
            log.info("[DataInitializer] Usuario COMERCIO creado: comercio@logired.com");
        }

        // ----------------------------------------------------------------
        // 3. Pedido inicial en estado CREADO
        // ----------------------------------------------------------------
        if (pedidosRepository.count() == 0) {
            Pedido pedido = Pedido.builder()
                    .comercioId(comercio.getId())
                    .direccionDestino("Av. Santa Fe 5000, CABA")
                    .estado(EstadoPedido.CREADO)
                    .build();
            pedidosRepository.save(pedido);
            log.info("[DataInitializer] Pedido inicial creado para comercioId={}",
                    comercio.getId());
        }

        log.info("[DataInitializer] Semilla de datos completada.");
    }
}
