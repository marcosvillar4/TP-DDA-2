package com.example.DA2Back.Seguridad.dto.perfilAdmin;


/*
 * Marca común de los datos propios de cada rol (comercio, repartidor, depósito)
 * tal como los ve el panel de administración de usuarios.
 *
 * Son DTOs propios del módulo de usuarios (no se reutilizan los *ResponseDTO de
 * Comercio/Depósito/Repartidor): así el contrato del admin puede evolucionar sin
 * tocar esos módulos, ni filtrar datos que el admin no necesita (itemsIds, pedidoActual...).
 */
public interface PerfilAdminDTO {

}
