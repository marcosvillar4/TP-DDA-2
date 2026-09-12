import { useEffect, useState } from "react";
import { getComercios, getDepositos, getInventarioPorComercio, getItemsPorInventario } from "../api/inventarioApi";
import { getPedidos, getPedidosPorComercio } from "../api/pedidosApi";
import { getUsuariosAdmin } from "../api/usuariosApi";

const ESTADOS_INICIALES = {
  CREADO: 0,
  ASIGNADO: 0,
  EN_CAMINO: 0,
  ENTREGADO: 0,
  CANCELADO: 0,
};

function contarPorEstado(pedidos) {
  return pedidos.reduce(
    (acc, p) => {
      acc[p.estado] = (acc[p.estado] ?? 0) + 1;
      return acc;
    },
    { ...ESTADOS_INICIALES }
  );
}

/**
 * Trae las métricas reales del Dashboard según el rol del usuario logueado.
 * Cada rol solo pide lo que realmente puede/necesita ver.
 * Si más adelante hay más endpoints disponibles, este es el único lugar
 * que hay que tocar para "activar" un widget nuevo.
 */
export function useDashboardMetrics(user) {
  const [metrics, setMetrics] = useState({});
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;

    let cancelado = false;

    async function cargar() {
      setLoading(true);
      const nuevasMetricas = {};

      if (user.rol === "ADMIN") {
        const [comercios, depositos, usuarios, pedidos] = await Promise.all([
          getComercios().catch(() => []),
          getDepositos().catch(() => []),
          getUsuariosAdmin().catch(() => []),
          getPedidos().catch(() => []),
        ]);

        nuevasMetricas.totalComercios = comercios.length;
        nuevasMetricas.totalDepositos = depositos.length;
        nuevasMetricas.totalRepartidores = usuarios.filter(
          (u) => u.rol === "REPARTIDOR"
        ).length;
        nuevasMetricas.totalPedidos = pedidos.length;
        nuevasMetricas.pedidosPorEstado = contarPorEstado(pedidos);
      }

      if (user.rol === "COMERCIO" && user.comercioId) {
        const pedidos = await getPedidosPorComercio(user.comercioId).catch(() => []);
        nuevasMetricas.totalPedidos = pedidos.length;
        nuevasMetricas.pedidosPorEstado = contarPorEstado(pedidos);

        const inventario = await getInventarioPorComercio(user.comercioId).catch(() => null);
        if (inventario) {
          const items = await getItemsPorInventario(inventario.id).catch(() => []);
          nuevasMetricas.totalItemsInventario = items.length;
          nuevasMetricas.stockTotal = items.reduce(
            (acc, it) => acc + (it.cantidad ?? 0),
            0
          );
        } else {
          nuevasMetricas.totalItemsInventario = 0;
          nuevasMetricas.stockTotal = 0;
        }
      }

      if (!cancelado) {
        setMetrics(nuevasMetricas);
        setLoading(false);
      }
    }

    cargar();

    return () => {
      cancelado = true;
    };
  }, [user?.rol, user?.comercioId]);

  return { metrics, loading };
}