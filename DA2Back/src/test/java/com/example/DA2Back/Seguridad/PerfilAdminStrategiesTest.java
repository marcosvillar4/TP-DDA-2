package com.example.DA2Back.Seguridad;

import com.example.DA2Back.Seguridad.dato.EstadoUsuario;
import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilComercioAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilDepositoAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilRepartidorAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilComercioAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilDepositoAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilRepartidorAdminDTO;
import com.example.DA2Back.Seguridad.excepcion.RecursoNoEncontradoException;
import com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin.PerfilComercioAdmin;
import com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin.PerfilDepositoAdmin;
import com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin.PerfilRepartidorAdmin;
import com.example.DA2Back.comercio.comercioDTOs.ComercioCreateDTO;
import com.example.DA2Back.comercio.comercioDTOs.ComercioResponseDTO;
import com.example.DA2Back.comercio.negocio.IComercio;
import com.example.DA2Back.deposito.dto.DepositoCreateDTO;
import com.example.DA2Back.deposito.dto.DepositoResponseDTO;
import com.example.DA2Back.deposito.negocio.IDeposito;
import com.example.DA2Back.repartidor.dato.EstadoRepartidor;
import com.example.DA2Back.repartidor.dto.RepartidorResponseDTO;
import com.example.DA2Back.repartidor.dto.RepartidorUpdateDTO;
import com.example.DA2Back.repartidor.negocio.IRepartidorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Estrategias de perfil por rol (patron Strategy) usadas por el ABM de administradores. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PerfilAdminStrategiesTest {

    @Mock private IComercio comercioService;
    @Mock private IDeposito depositoService;
    @Mock private IRepartidorService repartidorService;

    private final Usuario usuario = Usuario.builder().id(10L).estado(EstadoUsuario.VALIDADO).vehiculo("Moto").build();

    // ───────────── COMERCIO ─────────────

    @Test
    @DisplayName("PerfilComercioAdmin: rol COMERCIO y perfil con sus depositos y el maximo permitido")
    void comercio_obtener_ok() {
        PerfilComercioAdmin estrategia = new PerfilComercioAdmin(comercioService, depositoService);
        when(comercioService.obtenerPorUsuarioId(10L)).thenReturn(ComercioResponseDTO.builder()
                .id(1L).nombreComercial("Urban").razonSocial("Urban SA").cuit("20-1").direccion("C 1")
                .telefono("111").email("u@u.com").build());
        when(depositoService.obtenerPorComercio(1L)).thenReturn(List.of(DepositoResponseDTO.builder()
                .id(5L).nombre("Central").direccion("D 2").usuarioId(20L).responsable("Ana Paz")
                .usuarioEstado(EstadoUsuario.VALIDADO).build()));

        Optional<PerfilAdminDTO> res = estrategia.obtener(usuario);

        assertEquals(Rol.COMERCIO, estrategia.rol());
        PerfilComercioAdminDTO perfil = (PerfilComercioAdminDTO) res.orElseThrow();
        assertEquals("Urban", perfil.getNombreComercial());
        assertEquals(IDeposito.MAX_DEPOSITOS_POR_COMERCIO, perfil.getMaxDepositos());
        assertEquals(1, perfil.getDepositos().size());
        assertEquals("Ana Paz", perfil.getDepositos().get(0).getResponsable());
    }

    @Test
    @DisplayName("PerfilComercioAdmin: si no hay perfil de comercio devuelve vacio")
    void comercio_obtener_sinPerfil() {
        PerfilComercioAdmin estrategia = new PerfilComercioAdmin(comercioService, depositoService);
        when(comercioService.obtenerPorUsuarioId(10L)).thenThrow(new RecursoNoEncontradoException("sin comercio"));

        assertTrue(estrategia.obtener(usuario).isEmpty());
    }

    @Test
    @DisplayName("PerfilComercioAdmin.actualizar envia los seis campos sin espacios sobrantes")
    void comercio_actualizar_ok() {
        PerfilComercioAdmin estrategia = new PerfilComercioAdmin(comercioService, depositoService);
        when(comercioService.obtenerPorUsuarioId(10L)).thenReturn(ComercioResponseDTO.builder().id(1L).build());
        ActualizarPerfilComercioAdminDTO dto = new ActualizarPerfilComercioAdminDTO();
        dto.setNombreComercial("  Urban  ");
        dto.setRazonSocial(" Urban SA ");
        dto.setDireccion(" Calle 1 ");
        dto.setCuit(" 20-123 ");
        dto.setTelefono(" 111 ");
        dto.setEmail(" u@u.com ");

        estrategia.actualizar(usuario, dto);

        ArgumentCaptor<ComercioCreateDTO> captor = ArgumentCaptor.forClass(ComercioCreateDTO.class);
        verify(comercioService).actualizar(org.mockito.ArgumentMatchers.eq(1L), captor.capture());
        assertEquals("Urban", captor.getValue().getNombreComercial());
        assertEquals("20-123", captor.getValue().getCuit());
        assertEquals("u@u.com", captor.getValue().getEmail());
    }

    @Test
    @DisplayName("PerfilComercioAdmin.actualizar rechaza datos de otro rol")
    void comercio_actualizar_tipoIncorrecto() {
        PerfilComercioAdmin estrategia = new PerfilComercioAdmin(comercioService, depositoService);
        assertThrows(IllegalArgumentException.class,
                () -> estrategia.actualizar(usuario, new ActualizarPerfilDepositoAdminDTO()));
    }

    // ───────────── DEPOSITO ─────────────

    @Test
    @DisplayName("PerfilDepositoAdmin: con comercio asociado resuelve su dueño")
    void deposito_obtener_conComercio() {
        PerfilDepositoAdmin estrategia = new PerfilDepositoAdmin(depositoService, comercioService);
        when(depositoService.obtenerPorUsuarioId(10L)).thenReturn(DepositoResponseDTO.builder()
                .id(5L).nombre("Central").direccion("D 2").comercioId(1L).build());
        when(comercioService.obtenerPorId(1L)).thenReturn(ComercioResponseDTO.builder()
                .id(1L).nombreComercial("Urban").usuarioId(99L).build());

        PerfilDepositoAdminDTO perfil = (PerfilDepositoAdminDTO) estrategia.obtener(usuario).orElseThrow();

        assertEquals(Rol.DEPOSITO, estrategia.rol());
        assertEquals("Central", perfil.getNombre());
        assertEquals(99L, perfil.getComercio().getUsuarioId());
    }

    @Test
    @DisplayName("PerfilDepositoAdmin: sin comercio asociado el perfil no trae comercio")
    void deposito_obtener_sinComercio() {
        PerfilDepositoAdmin estrategia = new PerfilDepositoAdmin(depositoService, comercioService);
        when(depositoService.obtenerPorUsuarioId(10L)).thenReturn(DepositoResponseDTO.builder().id(5L).build());

        PerfilDepositoAdminDTO perfil = (PerfilDepositoAdminDTO) estrategia.obtener(usuario).orElseThrow();

        assertNull(perfil.getComercio());
    }

    @Test
    @DisplayName("PerfilDepositoAdmin: sin perfil de deposito devuelve vacio")
    void deposito_obtener_sinPerfil() {
        PerfilDepositoAdmin estrategia = new PerfilDepositoAdmin(depositoService, comercioService);
        when(depositoService.obtenerPorUsuarioId(10L)).thenThrow(new RecursoNoEncontradoException("sin deposito"));

        assertTrue(estrategia.obtener(usuario).isEmpty());
    }

    @Test
    @DisplayName("PerfilDepositoAdmin.actualizar recorta espacios; rechaza datos de otro rol")
    void deposito_actualizar() {
        PerfilDepositoAdmin estrategia = new PerfilDepositoAdmin(depositoService, comercioService);
        when(depositoService.obtenerPorUsuarioId(10L)).thenReturn(DepositoResponseDTO.builder().id(5L).build());
        ActualizarPerfilDepositoAdminDTO dto = new ActualizarPerfilDepositoAdminDTO();
        dto.setNombre("  Norte ");
        dto.setDireccion(" Av. 9 ");

        estrategia.actualizar(usuario, dto);

        ArgumentCaptor<DepositoCreateDTO> captor = ArgumentCaptor.forClass(DepositoCreateDTO.class);
        verify(depositoService).actualizar(org.mockito.ArgumentMatchers.eq(5L), captor.capture());
        assertEquals("Norte", captor.getValue().getNombre());
        assertEquals("Av. 9", captor.getValue().getDireccion());
        assertThrows(IllegalArgumentException.class,
                () -> estrategia.actualizar(usuario, new ActualizarPerfilComercioAdminDTO()));
    }

    // ───────────── REPARTIDOR ─────────────

    @Test
    @DisplayName("PerfilRepartidorAdmin: perfil con vehiculo del usuario; sin perfil devuelve vacio")
    void repartidor_obtener() {
        PerfilRepartidorAdmin estrategia = new PerfilRepartidorAdmin(repartidorService);
        when(repartidorService.obtenerPorUsuarioId(10L)).thenReturn(RepartidorResponseDTO.builder()
                .id(2L).patente("AB123CD").zona("CABA").estado(EstadoRepartidor.DISPONIBLE).activo(true).build());

        PerfilRepartidorAdminDTO perfil = (PerfilRepartidorAdminDTO) estrategia.obtener(usuario).orElseThrow();

        assertEquals(Rol.REPARTIDOR, estrategia.rol());
        assertEquals("Moto", perfil.getVehiculo());
        assertEquals("AB123CD", perfil.getPatente());
        assertTrue(perfil.isActivo());

        when(repartidorService.obtenerPorUsuarioId(11L)).thenThrow(new NoSuchElementException("sin perfil"));
        assertTrue(estrategia.obtener(Usuario.builder().id(11L).build()).isEmpty());
    }

    @Test
    @DisplayName("PerfilRepartidorAdmin.actualizar normaliza patente a mayusculas y actualiza el vehiculo del usuario")
    void repartidor_actualizar() {
        PerfilRepartidorAdmin estrategia = new PerfilRepartidorAdmin(repartidorService);
        when(repartidorService.obtenerPorUsuarioId(10L)).thenReturn(RepartidorResponseDTO.builder().id(2L).build());
        ActualizarPerfilRepartidorAdminDTO dto = new ActualizarPerfilRepartidorAdminDTO();
        dto.setPatente(" ab123cd ");
        dto.setZona(" Palermo ");
        dto.setVehiculo("Bicicleta");

        estrategia.actualizar(usuario, dto);

        ArgumentCaptor<RepartidorUpdateDTO> captor = ArgumentCaptor.forClass(RepartidorUpdateDTO.class);
        verify(repartidorService).actualizar(org.mockito.ArgumentMatchers.eq(2L), captor.capture());
        assertEquals("AB123CD", captor.getValue().getPatente());
        assertEquals("Palermo", captor.getValue().getZona());
        assertEquals("Bicicleta", usuario.getVehiculo());
        assertThrows(IllegalArgumentException.class,
                () -> estrategia.actualizar(usuario, new ActualizarPerfilComercioAdminDTO()));
    }
}
