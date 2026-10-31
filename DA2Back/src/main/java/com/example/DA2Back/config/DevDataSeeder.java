package com.example.DA2Back.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
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

/**
 * Datos de prueba para el perfil "dev": comercios, depositos, productos, stock y
 * repartidores con direcciones reales de la Ciudad Autonoma de Buenos Aires.
 *
 * Es idempotente: cada elemento se busca por email / SKU / usuario y solo se crea si
 * falta, asi que se puede ejecutar sobre una base que ya tenia el seed anterior.
 * Todas las cuentas usan la contrasena "admin123".
 */
@Slf4j
@Component
@Profile("dev")
@Order(2)
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private static final String PASSWORD = "admin123";

    // ------------------------------------------------------------------ datos

    private record ProductoSeed(String sku, String nombre, String descripcion, String categoria) { }

    /** stock: SKU -> cantidad disponible en ese deposito. */
    private record DepositoSeed(String email, String nombre, String apellido, String dni, String telefono,
                                String nombreDeposito, String direccion, Map<String, Integer> stock) { }

    private record ComercioSeed(String email, String nombre, String apellido, String dni, String telefono,
                                String nombreComercial, String razonSocial, String direccion, String cuit,
                                String emailComercio, List<ProductoSeed> productos, List<DepositoSeed> depositos) { }

    private record RepartidorSeed(String email, String nombre, String apellido, String dni, String telefono,
                                  String vehiculo, String patente, String zona, EstadoRepartidor estado) { }

    private static final List<ComercioSeed> COMERCIOS = List.of(
            // 1) Calzado. Conserva los datos del seed original (Urban Shoes + Deposito Central).
            new ComercioSeed("urbanshoes@da2back.com", "Urban", "Shoes", "20111222", "+54 11 4321-8765",
                    "Urban Shoes", "Urban Shoes S.A.", "Av. Corrientes 1234, CABA", "30-71234567-9",
                    "contacto@urbanshoes.com",
                    List.of(
                            new ProductoSeed("US-001", "Zapatillas Running X", "Calzado deportivo", "Calzado"),
                            new ProductoSeed("US-002", "Zapatillas Urbanas Classic", "Zapatilla casual de cuero", "Calzado"),
                            new ProductoSeed("US-003", "Botas de Cuero", "Botas de invierno", "Calzado"),
                            new ProductoSeed("US-004", "Sandalias de Verano", "Sandalias livianas", "Calzado"),
                            new ProductoSeed("US-005", "Medias Deportivas Pack x3", "Medias de algodon", "Accesorios")),
                    List.of(
                            new DepositoSeed("deposito.central@da2back.com", "Deposito", "Central", "20555666",
                                    "+54 11 4000-0001", "Deposito Central Almagro", "Castro Barros 450, CABA",
                                    Map.of("US-001", 50, "US-002", 30, "US-003", 12, "US-004", 3, "US-005", 100)),
                            new DepositoSeed("deposito.urban.crespo@da2back.com", "Deposito", "Villa Crespo", "20555667",
                                    "+54 11 4000-0002", "Deposito Villa Crespo", "Av. Warnes 650, CABA",
                                    Map.of("US-001", 20, "US-002", 0, "US-003", 25, "US-005", 60)))),

            // 2) Electronica en Palermo.
            new ComercioSeed("techzone@da2back.com", "Tech", "Zone", "20111223", "+54 11 4777-1010",
                    "TechZone Palermo", "TechZone Argentina S.R.L.", "Honduras 5200, CABA", "30-71345678-1",
                    "contacto@techzone.com",
                    List.of(
                            new ProductoSeed("TZ-001", "Auriculares Bluetooth", "Con cancelacion de ruido", "Electronica"),
                            new ProductoSeed("TZ-002", "Cargador USB-C 65W", "Carga rapida", "Electronica"),
                            new ProductoSeed("TZ-003", "Teclado Mecanico", "Switch rojo, layout espanol", "Perifericos"),
                            new ProductoSeed("TZ-004", "Mouse Inalambrico", "Ergonomico 2.4 GHz", "Perifericos"),
                            new ProductoSeed("TZ-005", "Parlante Portatil", "Resistente al agua", "Audio"),
                            new ProductoSeed("TZ-006", "Smartwatch Fit", "Monitor de actividad", "Electronica")),
                    List.of(
                            new DepositoSeed("deposito.techzone@da2back.com", "Deposito", "TechZone", "20555668",
                                    "+54 11 4000-0003", "Deposito Palermo", "Av. Juan B. Justo 2050, CABA",
                                    Map.of("TZ-001", 40, "TZ-002", 80, "TZ-003", 15, "TZ-004", 60,
                                            "TZ-005", 4, "TZ-006", 25)))),

            // 3) Libreria en San Telmo, deposito en Barracas.
            new ComercioSeed("libreriadelsur@da2back.com", "Libreria", "Del Sur", "20111224", "+54 11 4300-2020",
                    "Libreria del Sur", "Libreria del Sur S.A.", "Defensa 900, CABA", "30-71456789-2",
                    "contacto@libreriadelsur.com",
                    List.of(
                            new ProductoSeed("LS-001", "Cuaderno A5 Tapa Dura", "Rayado, 96 hojas", "Papeleria"),
                            new ProductoSeed("LS-002", "Set Lapiceras Gel x12", "Colores surtidos", "Papeleria"),
                            new ProductoSeed("LS-003", "Novela Clasica Edicion Bolsillo", "Literatura", "Libros"),
                            new ProductoSeed("LS-004", "Agenda 2027", "Dia por pagina", "Papeleria"),
                            new ProductoSeed("LS-005", "Mochila Escolar", "Con compartimento para notebook", "Accesorios")),
                    List.of(
                            new DepositoSeed("deposito.libreria@da2back.com", "Deposito", "Barracas", "20555669",
                                    "+54 11 4000-0004", "Deposito Barracas", "Av. Montes de Oca 1100, CABA",
                                    Map.of("LS-001", 120, "LS-002", 90, "LS-003", 45, "LS-004", 8, "LS-005", 30)))),

            // 4) Plantas y hogar en Belgrano, dos depositos.
            new ComercioSeed("casaverde@da2back.com", "Casa", "Verde", "20111225", "+54 11 4788-3030",
                    "Casa Verde", "Casa Verde Vivero S.R.L.", "Av. Cabildo 2040, CABA", "30-71567890-3",
                    "contacto@casaverde.com",
                    List.of(
                            new ProductoSeed("CV-001", "Maceta Ceramica 20cm", "Con plato", "Hogar"),
                            new ProductoSeed("CV-002", "Sustrato Universal 10L", "Enriquecido", "Jardin"),
                            new ProductoSeed("CV-003", "Suculenta Mix x3", "Plantas de interior", "Plantas"),
                            new ProductoSeed("CV-004", "Regadera 2L", "Plastico reforzado", "Jardin"),
                            new ProductoSeed("CV-005", "Set Herramientas de Jardin", "3 piezas de acero", "Jardin")),
                    List.of(
                            new DepositoSeed("deposito.casaverde.belgrano@da2back.com", "Deposito", "Belgrano", "20555670",
                                    "+54 11 4000-0005", "Deposito Belgrano", "Av. Monroe 2000, CABA",
                                    Map.of("CV-001", 70, "CV-002", 55, "CV-003", 35, "CV-004", 40, "CV-005", 18)),
                            new DepositoSeed("deposito.casaverde.caballito@da2back.com", "Deposito", "Caballito", "20555671",
                                    "+54 11 4000-0006", "Deposito Caballito", "Av. Rivadavia 5100, CABA",
                                    Map.of("CV-001", 25, "CV-002", 0, "CV-003", 60, "CV-005", 10)))),

            // 5) Deportes en Nunez, deposito en Flores.
            new ComercioSeed("deportesnunez@da2back.com", "Deportes", "Nunez", "20111226", "+54 11 4702-4040",
                    "Deportes Nunez", "Deportes Nunez S.A.", "Av. del Libertador 7000, CABA", "30-71678901-4",
                    "contacto@deportesnunez.com",
                    List.of(
                            new ProductoSeed("DN-001", "Pelota de Futbol N5", "Cosida a maquina", "Deportes"),
                            new ProductoSeed("DN-002", "Camiseta de Entrenamiento", "Secado rapido", "Indumentaria"),
                            new ProductoSeed("DN-003", "Guantes de Arquero", "Latex con proteccion", "Deportes"),
                            new ProductoSeed("DN-004", "Botella Termica 750ml", "Acero inoxidable", "Accesorios"),
                            new ProductoSeed("DN-005", "Colchoneta Yoga", "Antideslizante 6mm", "Fitness")),
                    List.of(
                            new DepositoSeed("deposito.deportes@da2back.com", "Deposito", "Flores", "20555672",
                                    "+54 11 4000-0007", "Deposito Flores", "Av. Rivadavia 7300, CABA",
                                    Map.of("DN-001", 55, "DN-002", 80, "DN-003", 14, "DN-004", 65, "DN-005", 22)))));

    private static final List<RepartidorSeed> REPARTIDORES = List.of(
            new RepartidorSeed("repartidor@da2back.com", "Juan", "Perez", "30123456", "+54 11 5555-4444",
                    "Moto Honda CG 150", "AB123CD", "Capital Federal", EstadoRepartidor.DISPONIBLE),
            new RepartidorSeed("maria.gonzalez@da2back.com", "Maria", "Gonzalez", "30123457", "+54 11 5555-4445",
                    "Moto Honda Wave 110", "AC456EF", "Palermo / Belgrano", EstadoRepartidor.DISPONIBLE),
            new RepartidorSeed("lucas.fernandez@da2back.com", "Lucas", "Fernandez", "30123458", "+54 11 5555-4446",
                    "Moto Yamaha YBR 125", "AD789GH", "Almagro / Caballito", EstadoRepartidor.DISPONIBLE),
            new RepartidorSeed("camila.rodriguez@da2back.com", "Camila", "Rodriguez", "30123459", "+54 11 5555-4447",
                    "Bicicleta electrica", "BICI-001", "Microcentro / Recoleta", EstadoRepartidor.DISPONIBLE),
            new RepartidorSeed("diego.martinez@da2back.com", "Diego", "Martinez", "30123460", "+54 11 5555-4448",
                    "Utilitario Renault Kangoo", "AF654KL", "Sur (San Telmo / Barracas)", EstadoRepartidor.DISPONIBLE),
            new RepartidorSeed("sofia.romero@da2back.com", "Sofia", "Romero", "30123461", "+54 11 5555-4449",
                    "Moto Zanella ZB 110", "AG987MN", "Norte (Nunez / Belgrano)", EstadoRepartidor.NO_DISPONIBLE));

    // ----------------------------------------------------------- dependencias

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
        String hash = passwordEncoder.encode(PASSWORD);
        int comerciosNuevos = 0;
        for (ComercioSeed seed : COMERCIOS) {
            if (sembrarComercio(seed, hash)) comerciosNuevos++;
        }
        int repartidoresNuevos = 0;
        for (RepartidorSeed seed : REPARTIDORES) {
            if (sembrarRepartidor(seed, hash)) repartidoresNuevos++;
        }
        log.info("Datos de prueba: {} comercios y {} repartidores nuevos (clave de todas las cuentas: {})",
                comerciosNuevos, repartidoresNuevos, PASSWORD);
    }

    // ---------------------------------------------------------------- comercio

    /** @return true si el comercio se creo en esta ejecucion. */
    private boolean sembrarComercio(ComercioSeed s, String hash) {
        Usuario usuario = usuarioRepository.findByEmail(s.email()).orElse(null);
        if (usuario == null) {
            usuario = usuarioRepository.save(nuevoUsuario(s.email(), hash, s.nombre(), s.apellido(),
                    s.dni(), s.telefono(), null, Rol.COMERCIO));
        }
        final Long usuarioId = usuario.getId();
        Comercio existente = comercioRepository.findByUsuarioId(usuarioId).orElse(null);
        boolean nuevo = existente == null;
        Comercio comercio = nuevo
                ? comercioRepository.save(Comercio.builder()
                        .nombreComercial(s.nombreComercial())
                        .razonSocial(s.razonSocial())
                        .direccion(s.direccion())
                        .CUIT(s.cuit())
                        .telefono(s.telefono())
                        .email(s.emailComercio())
                        .usuarioId(usuarioId)
                        .build())
                : existente;

        Inventario inventario = inventarioRepository.findByComercioId(comercio.getId()).orElse(null);
        if (inventario == null) {
            inventario = new Inventario();
            inventario.setComercio(comercio);
            inventario = inventarioRepository.save(inventario);
        }

        // Productos: SKU -> (producto, fue creado ahora)
        Map<String, Producto> productos = new java.util.HashMap<>();
        java.util.Set<String> skusNuevos = new java.util.HashSet<>();
        for (ProductoSeed p : s.productos()) {
            Producto prod = productoRepository.findByComercioIdAndSkuIgnoreCase(comercio.getId(), p.sku()).orElse(null);
            if (prod == null) {
                prod = productoRepository.save(new Producto(null, p.sku(), p.nombre(), p.descripcion(),
                        p.categoria(), EstadoProducto.ACTIVO, comercio));
                skusNuevos.add(p.sku());
            }
            productos.put(p.sku(), prod);
        }

        for (DepositoSeed d : s.depositos()) {
            sembrarDeposito(d, hash, comercio, inventario, productos, skusNuevos);
        }
        return nuevo;
    }

    private void sembrarDeposito(DepositoSeed d, String hash, Comercio comercio, Inventario inventario,
                                 Map<String, Producto> productos, java.util.Set<String> skusNuevos) {
        Usuario usuario = usuarioRepository.findByEmail(d.email()).orElse(null);
        if (usuario == null) {
            usuario = usuarioRepository.save(nuevoUsuario(d.email(), hash, d.nombre(), d.apellido(),
                    d.dni(), d.telefono(), null, Rol.DEPOSITO));
        }
        Deposito deposito = depositoRepository.findByUsuarioId(usuario.getId()).orElse(null);
        boolean depositoNuevo = deposito == null;
        if (depositoNuevo) {
            deposito = new Deposito();
            deposito.setNombre(d.nombreDeposito());
            deposito.setDireccion(d.direccion());
            deposito.setComercio(comercio);
            deposito.setUsuarioId(usuario.getId());
            deposito = depositoRepository.save(deposito);
        }
        // Se carga stock solo para producto o deposito nuevos, para no duplicar items ni pisar cantidades.
        for (Map.Entry<String, Integer> e : d.stock().entrySet()) {
            if (!depositoNuevo && !skusNuevos.contains(e.getKey())) continue;
            ItemInventario item = new ItemInventario();
            item.setInventario(inventario);
            item.setProducto(productos.get(e.getKey()));
            item.setDeposito(deposito);
            item.setCantidad(e.getValue());
            itemInventarioRepository.save(item);
        }
    }

    // -------------------------------------------------------------- repartidor

    private boolean sembrarRepartidor(RepartidorSeed s, String hash) {
        Usuario usuario = usuarioRepository.findByEmail(s.email()).orElse(null);
        if (usuario == null) {
            usuario = usuarioRepository.save(nuevoUsuario(s.email(), hash, s.nombre(), s.apellido(),
                    s.dni(), s.telefono(), s.vehiculo(), Rol.REPARTIDOR));
        }
        if (repartidorRepository.findByUsuarioId(usuario.getId()).isPresent()) return false;
        Repartidor r = new Repartidor();
        r.setPatente(s.patente());
        r.setZona(s.zona());
        r.setEstado(s.estado());
        r.setUsuarioId(usuario.getId());
        r.setActivo(true);
        repartidorRepository.save(r);
        return true;
    }

    private static Usuario nuevoUsuario(String email, String hash, String nombre, String apellido, String dni,
                                        String telefono, String vehiculo, Rol rol) {
        return Usuario.builder()
                .email(email)
                .password(hash)
                .nombre(nombre)
                .apellido(apellido)
                .DNI(dni)
                .telefono(telefono)
                .vehiculo(vehiculo)
                .rol(rol)
                .estado(EstadoUsuario.VALIDADO)
                .build();
    }
}
