/** Un ítem con este stock o menos se considera "stock bajo". */
export const UMBRAL_STOCK_BAJO = 5;

/**
 * Alertas operativas derivadas de datos reales del backend.
 * El backend no tiene un módulo de alertas ni marcas de tiempo, así que se
 * calculan acá a partir de inventario, pedidos y usuarios.
 *
 * @param {object} fuentes
 * @param {Array}  [fuentes.items]               ítems de inventario (ItemInventarioResponseDTO)
 * @param {Array}  [fuentes.pedidos]             pedidos (PedidoResponseDTO)
 * @param {number} [fuentes.usuariosPendientes]  usuarios EN_EVALUACION (solo ADMIN)
 * @param {boolean} [fuentes.mostrarComercio]    antepone el comercio en las alertas de stock
 */
export function construirAlertas({
  items = [],
  pedidos = [],
  usuariosPendientes = 0,
  mostrarComercio = false,
}) {
  const alertas = [];

  const detalleItem = (it) => {
    const base = `${it.productoNombre ?? `Producto Nº ${it.productoId}`} – ${
      it.depositoNombre ?? `Depósito Nº ${it.depositoId}`
    }`;
    return mostrarComercio && it.comercioNombre ? `${it.comercioNombre} · ${base}` : base;
  };

  items
    .filter((it) => (it.cantidad ?? 0) <= 0)
    .forEach((it) =>
      alertas.push({
        nivel: "critico",
        tipo: "CRÍTICO",
        titulo: "Sin stock",
        desc: `${detalleItem(it)} sin stock disponible`,
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

  items
    .filter((it) => it.cantidad > 0 && it.cantidad <= UMBRAL_STOCK_BAJO)
    .forEach((it) =>
      alertas.push({
        nivel: "advertencia",
        tipo: "ADVERTENCIA",
        titulo: "Stock bajo",
        desc: `${detalleItem(it)}: ${it.cantidad} unidad${it.cantidad !== 1 ? "es" : ""} restante${it.cantidad !== 1 ? "s" : ""}`,
      })
    );

  return alertas;
}
