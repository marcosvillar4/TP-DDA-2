package com.example.DA2Back.notificacion.dato;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioIdAndCanalOrderByFechaCreacionDesc(
            Long usuarioId, CanalNotificacion canal);

    long countByUsuarioIdAndCanalAndLeidaFalse(Long usuarioId, CanalNotificacion canal);

    List<Notificacion> findTop50ByCanalAndEnviadaFalseOrderByFechaCreacionAsc(
            CanalNotificacion canal);
}
