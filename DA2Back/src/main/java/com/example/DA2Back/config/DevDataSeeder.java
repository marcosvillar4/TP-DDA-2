package com.example.DA2Back.config;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.InventarioRepository;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final ComercioRepository comercioRepository;
    private final DepositoRepository depositoRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (comercioRepository.count() == 0) {

            // Usuario dueño de Urban Shoes
            Usuario uComercio1 = usuarioRepository.save(Usuario.builder()
                    .email("urbanshoes@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Urban")
                    .apellido("Shoes")
                    .DNI("20111222")
                    .telefono("+54 11 4321-8765")
                    .rol(Rol.COMERCIO)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());

            Comercio c1 = comercioRepository.save(Comercio.builder()
                    .nombreComercial("Urban Shoes")
                    .razonSocial("Urban Shoes S.A.")
                    .direccion("Av. Corrientes 1234, CABA")
                    .CUIT("30-71234567-9")
                    .telefono("+54 11 4321-8765")
                    .email("contacto@urbanshoes.com")
                    .usuarioId(uComercio1.getId())
                    .build());

            // Usuario dueño de ElectroTech
            Usuario uComercio2 = usuarioRepository.save(Usuario.builder()
                    .email("electrotech@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Electro")
                    .apellido("Tech")
                    .DNI("20333444")
                    .telefono("+54 11 4822-1100")
                    .rol(Rol.COMERCIO)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());

            Comercio c2 = comercioRepository.save(Comercio.builder()
                    .nombreComercial("ElectroTech Store")
                    .razonSocial("ElectroTech S.R.L.")
                    .direccion("Av. Santa Fe 2500, CABA")
                    .CUIT("30-75558889-1")
                    .telefono("+54 11 4822-1100")
                    .email("ventas@electrotech.com")
                    .usuarioId(uComercio2.getId())
                    .build());

            log.info("Comercios de prueba sembrados: {}, {}", c1.getNombreComercial(), c2.getNombreComercial());

            // Usuario para depósito 1
            Usuario uDep1 = usuarioRepository.save(Usuario.builder()
                    .email("deposito.central@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Depósito")
                    .apellido("Central")
                    .DNI("20555666")
                    .telefono("+54 11 4000-0001")
                    .rol(Rol.DEPOSITO)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());

            Deposito d1 = new Deposito();
            d1.setNombre("Depósito Central Almagro");
            d1.setDireccion("Castro Barros 450, CABA");
            d1.setComercio(c1);
            d1.setUsuarioId(uDep1.getId());
            depositoRepository.save(d1);

            // Usuario para depósito 2
            Usuario uDep2 = usuarioRepository.save(Usuario.builder()
                    .email("deposito.norte@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Depósito")
                    .apellido("Norte")
                    .DNI("20777888")
                    .telefono("+54 11 4000-0002")
                    .rol(Rol.DEPOSITO)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());

            Deposito d2 = new Deposito();
            d2.setNombre("Depósito Norte Belgrano");
            d2.setDireccion("Juramento 1800, CABA");
            d2.setComercio(c1);
            d2.setUsuarioId(uDep2.getId());
            depositoRepository.save(d2);

            log.info("Depósitos sembrados para {}", c1.getNombreComercial());

            Producto p1 = new Producto(null, "Zapatillas Running X", "Calzado deportivo talle 42", 85000.0);
            Producto p2 = new Producto(null, "Mochila Urbana Tech", "Mochila impermeable porta notebook", 45000.0);
            Producto p3 = new Producto(null, "Auriculares Bluetooth Pro", "Cancelación de ruido activa", 62000.0);

            productoRepository.save(p1);
            productoRepository.save(p2);
            productoRepository.save(p3);

            log.info("Productos de prueba sembrados");

            Inventario inv = new Inventario();
            inv.setComercio(c1);
            inventarioRepository.save(inv);

            log.info("Inventario sembrado para {}", c1.getNombreComercial());
        }
    }
}
