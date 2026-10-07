/**
 * Alertas operativas derivadas de datos reales del backend.
 * Las alertas de stock bajo ya vienen calculadas por el backend según
 * ItemInventario.stockMinimo. El frontend solo las adapta al formato visual.
 *
 * @param {object} fuentes
 * @param {Array}  [fuentes.alertasStock]        alertas derivadas del backend
 * @param {Array}  [fuentes.pedidos]             pedidos (PedidoResponseDTO)
 * @param {number} [fuentes.usuariosPendientes]  usuarios EN_EVALUACION (solo ADMIN)
 * @param {boolean} [fuentes.mostrarComercio]    antepone el comercio en las alertas de stock
 */
export function construirAlertas({
  alertasStock = [],
  pedidos = [],
  usuariosPendientes = 0,
  mostrarComercio = false,
}) {
  const alertas = [];

  const detalleStock = (alerta) => {
    const base = `${alerta.productoNombre ?? `Producto Nº ${alerta.productoId}`} – ${
      alerta.depositoNombre ?? `Depósito Nº ${alerta.depositoId}`
    }`;
    return mostrarComercio && alerta.comercioNombre ? `${alerta.comercioNombre} · ${base}` : base;
  };

  alertasStock
    .forEach((alerta) =>
      alertas.push({
        nivel: (alerta.cantidad ?? 0) <= 0 ? "critico" : "advertencia",
        tipo: (alerta.cantidad ?? 0) <= 0 ? "CRÍTICO" : "ADVERTENCIA",
        titulo: (alerta.cantidad ?? 0) <= 0 ? "Sin stock" : "Stock bajo",
        desc: `${detalleStock(alerta)}: ${alerta.cantidad ?? 0} unidad${alerta.cantidad !== 1 ? "es" : ""} disponible${alerta.cantidad !== 1 ? "s" : ""} (mínimo ${alerta.stockMinimo ?? "—"})`,
      })
    );

  const sinAsignar = pedidos.filter((p) => p.estado === "CREADO" && !p.repartidorId).length;
  if (sinAsignar > 0) {
    alertas.push({
      nivel: "advertencia",
      tipo: "ADVERTENCIA",
      titulo: "Pedidos sin repartidor",
      desc: `${sinAsignar} pedido${sinAsignar !== 1 ? "s" : ""} esperando asignación de repartidor`,
    });
  }

  if (usuariosPendientes > 0) {
    alertas.push({
      nivel: "advertencia",
      tipo: "ADVERTENCIA",
      titulo: "Cuentas por validar",
      desc: `${usuariosPendientes} cuenta${usuariosPendientes !== 1 ? "s" : ""} en evaluación pendiente${usuariosPendientes !== 1 ? "s" : ""} de validación`,
    });
  }

  return alertas;
}
