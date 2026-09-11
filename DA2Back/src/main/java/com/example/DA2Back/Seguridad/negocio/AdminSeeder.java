package com.example.DA2Back.Seguridad.negocio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
 
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
 
/*
 Crea un usuario ADMIN al levantar la aplicación, solo si todavía
 no existe ningún usuario con ese rol. Es la única vía para tener
 un admin, ya que el registro público (/auth/register) lo rechaza
 a propósito.

 Las credenciales deben estar en application.properties para no
 hardcodearlas en el código:

   admin.seed.username=admin
   admin.seed.email=admin@da2back.com
   admin.seed.password=CambiarEstaClave123

 Una vez que el admin ya fue creado, este runner no hace nada más
 en los próximos arranques.*/
 
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioFactory usuarioFactory;

    @Value("${admin.seed.email:admin@da2back.com}")
    private String adminEmail;

    @Value("${admin.seed.password:admin123}")
    private String adminPassword;

    @Value("${admin.seed.nombre:Admin}")
    private String adminNombre;

    @Value("${admin.seed.apellido:LogiRed}")
    private String adminApellido;

    @Value("${admin.seed.dni:00000000}")
    private String adminDni;

    @Value("${admin.seed.telefono:0000000000}")
    private String adminTelefono;

    @Override
    public void run(String... args) {

        boolean existeAdmin = usuarioRepository.findAll().stream()
                .anyMatch(u -> u.getRol() == Rol.ADMIN);

        if (existeAdmin) {
            return;
        }

        if (usuarioRepository.existsByEmail(adminEmail)) {
            log.warn("No se creó el admin: ya existe un usuario con el email {}", adminEmail);
            return;
        }

        Usuario admin = usuarioFactory.crearAdministrador(
                adminEmail,
                adminPassword,
                adminNombre,
                adminApellido,
                adminDni,
                adminTelefono
        );

        usuarioRepository.save(admin);

        log.info("Usuario ADMIN creado -> email: {} / password: {}", adminEmail, adminPassword);
    }
}
