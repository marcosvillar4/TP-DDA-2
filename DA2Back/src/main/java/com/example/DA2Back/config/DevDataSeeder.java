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
import com.example.DA2Back.inventario.dato.ItemInventario;
import com.example.DA2Back.inventario.dato.ItemInventarioRepository;
import com.example.DA2Back.producto.dato.EstadoProducto;
import com.example.DA2Back.producto.dato.Producto;
import com.example.DA2Back.producto.dato.ProductoRepository;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dato.Repartidor;
import com.example.DA2Back.repartidor.dato.RepartidorRepository;
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
    private final ItemInventarioRepository itemInventarioRepository;
    private final RepartidorRepository repartidorRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (comercioRepository.count() == 0) {
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

            Usuario uDep1 = usuarioRepository.save(Usuario.builder()
                    .email("deposito.central@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Deposito")
                    .apellido("Central")
                    .DNI("20555666")
                    .telefono("+54 11 4000-0001")
                    .rol(Rol.DEPOSITO)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());

            Deposito d1 = new Deposito();
            d1.setNombre("Deposito Central Almagro");
            d1.setDireccion("Castro Barros 450, CABA");
            d1.setComercio(c1);
            d1.setUsuarioId(uDep1.getId());
            depositoRepository.save(d1);

            Producto p1 = new Producto(null, "US-001", "Zapatillas Running X", "Calzado deportivo", "Calzado", EstadoProducto.ACTIVO, c1);
            productoRepository.save(p1);

            Inventario inv = new Inventario();
            inv.setComercio(c1);
            inventarioRepository.save(inv);

            ItemInventario item1 = new ItemInventario();
            item1.setInventario(inv);
            item1.setProducto(p1);
            item1.setDeposito(d1);
            item1.setCantidad(50);
            itemInventarioRepository.save(item1);

            Usuario uRepartidor = usuarioRepository.save(Usuario.builder()
                    .email("repartidor@da2back.com")
                    .password(passwordEncoder.encode("admin123"))
                    .nombre("Juan")
                    .apellido("Perez")
                    .DNI("30123456")
                    .telefono("+54 11 5555-4444")
                    .rol(Rol.REPARTIDOR)
                    .estado(EstadoUsuario.VALIDADO)
                    .build());

            Repartidor rep = new Repartidor();
            rep.setPatente("AB123CD");
            rep.setZona("Capital Federal");
            rep.setEstado(EstadoRepartidor.DISPONIBLE);
            rep.setUsuario(uRepartidor);
            rep.setActivo(true);
            repartidorRepository.save(rep);

            log.info("Datos sembrados");
        }
    }
}
