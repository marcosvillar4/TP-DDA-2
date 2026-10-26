package com.example.DA2Back.integration;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dato.UsuarioRepository;
import com.example.DA2Back.Seguridad.negocio.JwtService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SEC-W06 (OWASP A01:2025 Control de Acceso / ASVS 5.0):
 * La autorización se valida en el servidor en cada request; ocultar un botón no cuenta.
 * Un usuario con permisos insuficientes que conoce la URL o manipuló el cliente
 * envía la petición HTTP directa hacia el endpoint protegido y recibe 403 Forbidden.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecW06AutorizacionServidorIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ComercioRepository comercioRepository;
    @Autowired private DepositoRepository depositoRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private ItemInventarioRepository itemInventarioRepository;

    private String tokenComercioA;
    private String tokenDepositoA;
    private String tokenRepartidor;

    private Comercio comercioB;
    private Deposito depositoB;
    private Producto productoB;
    private ItemInventario itemB;

    @BeforeEach
    void prepararEscenario() {
        String sufijo = UUID.randomUUID().toString().substring(0, 8);

        Usuario usuarioComercioA = crearUsuario("comercioA." + sufijo + "@secw06.test", "10" + sufijo, Rol.COMERCIO);
        Usuario usuarioComercioB = crearUsuario("comercioB." + sufijo + "@secw06.test", "20" + sufijo, Rol.COMERCIO);
        Usuario usuarioDepositoA = crearUsuario("depositoA." + sufijo + "@secw06.test", "30" + sufijo, Rol.DEPOSITO);
        Usuario usuarioDepositoB = crearUsuario("depositoB." + sufijo + "@secw06.test", "40" + sufijo, Rol.DEPOSITO);
        Usuario usuarioRepartidor = crearUsuario("repartidor." + sufijo + "@secw06.test", "50" + sufijo, Rol.REPARTIDOR);

        Comercio comercioA = crearComercio(usuarioComercioA, "CUIT-A-" + sufijo);
        comercioB = crearComercio(usuarioComercioB, "CUIT-B-" + sufijo);

        crearDeposito(comercioA, usuarioDepositoA, "Depósito A");
        depositoB = crearDeposito(comercioB, usuarioDepositoB, "Depósito B");

        productoB = productoRepository.save(new Producto(
                null, "SKU-B-" + sufijo, "Producto B", "Desc B", "General", EstadoProducto.ACTIVO, comercioB));

        Inventario invB = new Inventario();
        invB.setComercio(comercioB);
        invB = inventarioRepository.save(invB);

        ItemInventario item = new ItemInventario();
        item.setInventario(invB);
        item.setProducto(productoB);
        item.setDeposito(depositoB);
        item.setCantidad(25);
        item.setStockMinimo(5);
        itemB = itemInventarioRepository.save(item);

        tokenComercioA = jwtService.generateToken(usuarioComercioA);
        tokenDepositoA = jwtService.generateToken(usuarioDepositoA);
        tokenRepartidor = jwtService.generateToken(usuarioRepartidor);
    }

    @Test
    @DisplayName("SEC-W06: Comercio A no puede modificar ni desactivar productos de Comercio B por request directo")
    void secW06_comercioNoPuedeModificarProductoDeOtroComercio() throws Exception {
        String updateBody = """
                {
                  "nombre": "Producto Hackeado",
                  "descripcion": "Intento de IDOR",
                  "categoria": "General"
                }
                """;

        // Intento directo de PUT /productos/{id} sobre producto de Comercio B -> 403 Forbidden
        mockMvc.perform(put("/productos/" + productoB.getId())
                        .header("Authorization", "Bearer " + tokenComercioA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        // Intento directo de PATCH /productos/{id}/desactivar sobre producto de Comercio B -> 403 Forbidden
        mockMvc.perform(patch("/productos/" + productoB.getId() + "/desactivar")
                        .header("Authorization", "Bearer " + tokenComercioA))
                .andExpect(status().isForbidden());

        // Intento directo de POST /productos inyectando el comercioId de Comercio B -> 403 Forbidden
        String createBody = """
                {
                  "sku": "SKU-INTRUSO",
                  "nombre": "Producto Intruso",
                  "descripcion": "Creado sin pasar por UI",
                  "categoria": "General",
                  "comercioId": %d
                }
                """.formatted(comercioB.getId());

        mockMvc.perform(post("/productos")
                        .header("Authorization", "Bearer " + tokenComercioA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isForbidden());

        // Verifica que el producto de Comercio B no sufrió alteraciones
        Producto intacto = productoRepository.findById(productoB.getId()).orElseThrow();
        assertEquals("Producto B", intacto.getNombre());
        assertEquals(EstadoProducto.ACTIVO, intacto.getEstado());
    }

    @Test
    @DisplayName("SEC-W06: Comercio A no puede alterar ni borrar stock del inventario de Comercio B")
    void secW06_comercioNoPuedeAlterarStockDeOtroComercio() throws Exception {
        mockMvc.perform(put("/items-inventario/" + itemB.getId() + "/cantidad")
                        .param("cantidad", "0")
                        .header("Authorization", "Bearer " + tokenComercioA))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/items-inventario/" + itemB.getId())
                        .header("Authorization", "Bearer " + tokenComercioA))
                .andExpect(status().isForbidden());

        // Verifica que el stock de Comercio B permanece en 25
        assertEquals(25, itemInventarioRepository.findById(itemB.getId()).orElseThrow().getCantidad());
    }

    @Test
    @DisplayName("SEC-W06: Depósito A no puede modificar los datos del Depósito B manipulando la URL")
    void secW06_depositoNoPuedeModificarOtroDeposito() throws Exception {
        String body = """
                {
                  "nombre": "Deposito Alterado",
                  "direccion": "Calle Falsa 999"
                }
                """;

        mockMvc.perform(put("/depositos/" + depositoB.getId())
                        .header("Authorization", "Bearer " + tokenDepositoA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());

        assertEquals("Depósito B", depositoRepository.findById(depositoB.getId()).orElseThrow().getNombre());
    }

    @Test
    @DisplayName("SEC-W06: Usuarios sin rol ADMIN que invocan endpoints administrativos directos reciben 403 Forbidden")
    void secW06_accesoDirectoAEndpointsAdminSinPermisosRecibe403() throws Exception {
        for (String token : new String[]{tokenComercioA, tokenDepositoA, tokenRepartidor}) {
            mockMvc.perform(get("/usuarios/admin/pendientes")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/comercios/" + comercioB.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
    }

    private Usuario crearUsuario(String email, String dni, Rol rol) {
        return usuarioRepository.save(Usuario.builder()
                .email(email)
                .password("encoded-pass")
                .nombre("Test")
                .apellido(rol.name())
                .DNI(dni)
                .telefono("1100000000")
                .rol(rol)
                .estado(EstadoUsuario.VALIDADO)
                .build());
    }

    private Comercio crearComercio(Usuario dueno, String cuit) {
        return comercioRepository.save(Comercio.builder()
                .usuarioId(dueno.getId())
                .nombreComercial("Comercio " + cuit)
                .razonSocial("RS " + cuit)
                .direccion("Av. Siempreviva 123")
                .CUIT(cuit)
                .telefono("1122334455")
                .email(dueno.getEmail())
                .build());
    }

    private Deposito crearDeposito(Comercio comercio, Usuario encargado, String nombre) {
        Deposito d = new Deposito();
        d.setNombre(nombre);
        d.setDireccion("Av. Depósito 100");
        d.setComercio(comercio);
        d.setUsuarioId(encargado.getId());
        return depositoRepository.save(d);
    }
}
