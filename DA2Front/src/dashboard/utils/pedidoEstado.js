/**
 * Presentación de EstadoPedido (enum del backend):
 * CREADO, ASIGNADO, EN_CAMINO, ENTREGADO, CANCELADO.
 */
export const ESTADOS_PEDIDO = {
  CREADO: { label: "Creado", badge: "badge-pendiente", color: "var(--color-amber)" },
  ASIGNADO: { label: "Asignado", badge: "badge-preparando", color: "var(--color-orange)" },
  EN_CAMINO: { label: "En camino", badge: "badge-transito", color: "var(--color-violet)" },
  ENTREGADO: { label: "Entregado", badge: "badge-entregado", color: "var(--color-green)" },
  CANCELADO: { label: "Cancelado", badge: "badge-cancelado", color: "var(--color-red)" },
};

export const ORDEN_ESTADOS_PEDIDO = ["CREADO", "ASIGNADO", "EN_CAMINO", "ENTREGADO", "CANCELADO"];

export function contarPorEstado(pedidos) {
  const conteo = Object.fromEntries(ORDEN_ESTADOS_PEDIDO.map((e) => [e, 0]));
  pedidos.forEach((p) => {
    conteo[p.estado] = (conteo[p.estado] ?? 0) + 1;
  });
  return conteo;
}
