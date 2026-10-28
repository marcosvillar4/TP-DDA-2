package com.example.DA2Back.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.DA2Back.Seguridad.negocio.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

/**
 * Seguridad HTTP de la API.
 *
 * Criterio:
 *  - ADMIN conserva las operaciones administrativas y es el unico rol con
 *    acceso a lo que no este listado explicitamente.
 *  - El resto de los roles accede solo a lo que necesita su pantalla.
 *  - Spring usa la PRIMERA regla que coincide, por eso las reglas
 *    específicas van antes que las generales.
 *  - Los chequeos de "dueño del recurso" (ej. un COMERCIO solo ve lo suyo)
 *    se hacen en la capa de negocio.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String COMERCIO = "COMERCIO";
    private static final String DEPOSITO = "DEPOSITO";
    private static final String REPARTIDOR = "REPARTIDOR";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth

                // ───────── Público ─────────
                .requestMatchers("/auth/**", "/error").permitAll()

                // ───────── Usuarios ─────────
                .requestMatchers("/usuarios/admin/**").hasRole(ADMIN)
                .requestMatchers(GET, "/usuarios/me").authenticated()

                // ───────── Comercios ─────────
                .requestMatchers(GET, "/comercios/me").hasAnyRole(ADMIN, COMERCIO)
                // Datos de referencia (nombres de comercio en pedidos/seguimiento): todos los roles.
                .requestMatchers(GET, "/comercios", "/comercios/*").authenticated()
                .requestMatchers(PUT, "/comercios/*").hasAnyRole(ADMIN, COMERCIO)
                .requestMatchers("/comercios/**").hasRole(ADMIN)

                // ───────── Depósitos ─────────
                .requestMatchers(GET, "/depositos/me").hasRole(DEPOSITO)
                .requestMatchers(GET, "/depositos/comercio/*").hasAnyRole(ADMIN, COMERCIO)
                // Un COMERCIO da de alta depósitos para SU comercio (se vincula al usuario autenticado).
                .requestMatchers(POST, "/depositos").hasRole(COMERCIO)
                .requestMatchers(PATCH, "/depositos/asociar").hasRole(ADMIN)
                .requestMatchers(PUT, "/depositos/*").hasRole(ADMIN)
                .requestMatchers("/depositos/**").hasRole(ADMIN)

                // ───────── Productos ─────────
                .requestMatchers("/productos/**").hasAnyRole(ADMIN, COMERCIO)

                // ───────── Inventarios e ítems ─────────
                .requestMatchers("/inventarios/**").hasAnyRole(ADMIN, COMERCIO)
                .requestMatchers(GET, "/items-inventario").hasRole(ADMIN)
                // El alcance (todo / su comercio / su depósito) lo resuelve el servicio según el rol.
                .requestMatchers(GET, "/items-inventario/alertas-stock")
                    .hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                // DEPOSITO solo ve el inventario de su depósito (validado en el servicio).
                .requestMatchers(GET, "/items-inventario/deposito/*")
                    .hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                .requestMatchers(PATCH, "/items-inventario/*/stock-minimo")
                    .hasAnyRole(ADMIN, COMERCIO)
                .requestMatchers("/items-inventario/**").hasAnyRole(ADMIN, COMERCIO)

                // ───────── Pedidos ─────────
                .requestMatchers(POST, "/api/pedidos").hasAnyRole(ADMIN, COMERCIO)
                .requestMatchers(POST, "/api/pedidos/cotizar").hasAnyRole(ADMIN, COMERCIO)
                .requestMatchers(GET, "/api/pedidos/*/ruta").hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                .requestMatchers(GET, "/api/pedidos").hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                .requestMatchers(GET, "/api/pedidos/comercio/*").hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                .requestMatchers(GET, "/api/pedidos/estado/*", "/api/pedidos/pendientes")
                    .hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                // El servicio limita las consultas al comercio o deposito autenticado.
                .requestMatchers(GET, "/api/pedidos/*").hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                .requestMatchers(PATCH, "/api/pedidos/*/listo-para-retirar")
                    .hasAnyRole(ADMIN, COMERCIO, DEPOSITO)
                .requestMatchers(PATCH, "/api/pedidos/*/asignar/*").hasRole(ADMIN)
                .requestMatchers(PATCH, "/api/pedidos/*/retirar",
                                        "/api/pedidos/*/iniciar-viaje",
                                        "/api/pedidos/*/entregar")
                    .hasRole(ADMIN)
                .requestMatchers(DELETE, "/api/pedidos/*/cancelar").hasAnyRole(ADMIN, COMERCIO)

                // ───────── Repartidores ─────────
                .requestMatchers("/repartidores/**").hasRole(ADMIN)
                .requestMatchers("/repartidor/**").hasRole(REPARTIDOR)

                // Notificaciones propias del usuario autenticado.
                .requestMatchers("/notificaciones/**").authenticated()

                // Todo lo demás: solo ADMIN.
                .anyRequest().hasRole(ADMIN)
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
