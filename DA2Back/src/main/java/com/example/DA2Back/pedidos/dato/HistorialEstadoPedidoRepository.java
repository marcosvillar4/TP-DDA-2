package com.example.DA2Back.pedidos.dato;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialEstadoPedidoRepository extends JpaRepository<HistorialEstadoPedido, Long> {
    List<HistorialEstadoPedido> findByPedidoIdOrderByFechaHoraDesc(Long pedidoId);
}
