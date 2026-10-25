package com.example.DA2Back.Seguridad.negocio.StrategyPerfilAdmin;

import java.util.Optional;

import com.example.DA2Back.Seguridad.dato.Rol;
import com.example.DA2Back.Seguridad.dato.Usuario;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.ActualizarPerfilAdminDTO;
import com.example.DA2Back.Seguridad.dto.perfilAdmin.PerfilAdminDTO;


/**
 * Estrategia por rol para leer y modificar los datos propios de un usuario
 * (comercio, repartidor, depósito) desde el panel de administración.
 *
 * Cada implementación delega en el servicio público de su módulo (IComercio,
 * IRepartidorService, IDeposito): el módulo de usuarios no conoce sus repositorios
 * ni sus entidades. Para soportar un rol nuevo alcanza con agregar una clase @Component.
 */
public interface IPerfilAdmin {
    Rol rol();
 
    /** Vacío si el usuario todavía no tiene perfil creado. */
    Optional<PerfilAdminDTO> obtener(Usuario usuario);
 
    /**
     * Aplica los cambios en el módulo dueño del dato. Debe invocarse dentro de una
     * transacción: así, si algo falla, no queda guardada una parte de los cambios.
     * Puede modificar el propio Usuario (p. ej. el vehículo); quien llama lo persiste.
     */
    void actualizar(Usuario usuario, ActualizarPerfilAdminDTO dto);
}
