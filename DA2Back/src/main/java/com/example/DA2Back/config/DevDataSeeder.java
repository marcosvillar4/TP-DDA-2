package com.example.DA2Back.config;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.comercio.dato.Comercio;
import com.example.DA2Back.comercio.dato.ComercioRepository;
import com.example.DA2Back.deposito.dato.Deposito;
import com.example.DA2Back.deposito.dato.DepositoRepository;
import com.example.DA2Back.inventario.dato.Inventario;
import com.example.DA2Back.inventario.dato.InventarioRepository;
import com.example.DA2Back.producto.dato.EstadoProducto;
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
            Comercio c1 = comercioRepository.save(Comercio.builder()
                    .nombre("Urban Shoes S.A.")
                    .direccion("Av. Corrientes 1234, CABA")
                    .telefono("+54 11 4321-8765")
                    .email("contacto@urbanshoes.com")
                    .build());

            Comercio c2 = comercioRepository.save(Comercio.builder()
                    .nombre("ElectroTech Store")
                    .direccion("Av. Santa Fe 2500, CABA")
                    .telefono("+54 11 4822-1100")
                    .email("ventas@electrotech.com")
                    .build());

            log.info("Comercios de prueba sembrados: {}, {}", c1.getNombre(), c2.getNombre());

            // Crear usuario para depósito 1
            Usuario uDep1 = usuarioRepository.save(Usuario.builder()
                    .username("deposito.central")
                    .email("deposito.central@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(Rol.DEPOSITO)
                    .activo(true)
                    .build());

            Deposito d1 = new Deposito();
            d1.setNombre("Depósito Central Almagro");
            d1.setDireccion("Castro Barros 450, CABA");
            d1.setComercio(c1);
            d1.setUsuario(uDep1);
            depositoRepository.save(d1);

            // Crear usuario para depósito 2
            Usuario uDep2 = usuarioRepository.save(Usuario.builder()
                    .username("deposito.norte")
                    .email("deposito.norte@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .rol(Rol.DEPOSITO)
                    .activo(true)
                    .build());

            Deposito d2 = new Deposito();
            d2.setNombre("Depósito Norte Belgrano");
            d2.setDireccion("Juramento 1800, CABA");
            d2.setComercio(c1);
            d2.setUsuario(uDep2);
            depositoRepository.save(d2);

            log.info("Depósitos sembrados para {}", c1.getNombre());

            Producto p1 = new Producto(
                    null,
                    "US-001",
                    "Zapatillas Running X",
                    "Calzado deportivo talle 42",
                    "Calzado",
                    EstadoProducto.ACTIVO,
                    c1
            );
            Producto p2 = new Producto(
                    null,
                    "US-002",
                    "Mochila Urbana Tech",
                    "Mochila impermeable porta notebook",
                    "Accesorios",
                    EstadoProducto.ACTIVO,
                    c1
            );
            Producto p3 = new Producto(
                    null,
                    "ET-001",
                    "Auriculares Bluetooth Pro",
                    "Cancelación de ruido activa",
                    "Electrónica",
                    EstadoProducto.ACTIVO,
                    c2
            );

            productoRepository.save(p1);
            productoRepository.save(p2);
            productoRepository.save(p3);

            log.info("Productos de prueba sembrados");

            // Crear inventario para Urban Shoes
            Inventario inv = new Inventario();
            inv.setComercio(c1);
            inventarioRepository.save(inv);

            log.info("Inventario sembrado para {}", c1.getNombre());
        }
    }
}
