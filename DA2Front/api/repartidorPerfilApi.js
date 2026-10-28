import { request } from "./httpClient";

/** Perfil del repartidor autenticado (rol REPARTIDOR), con su pedido actual. */
export function getMiPerfilRepartidor() {
  return request("/repartidor/me");
}

/**
 * Recorrido pendiente (paradas ordenadas) del repartidor autenticado.
 * lat/lng son opcionales: su ubicacion actual como punto de partida.
 */
export function getMiRuta(lat, lng) {
  const params = new URLSearchParams();
  if (lat != null && lng != null) {
    params.set("lat", lat);
    params.set("lng", lng);
  }
  const query = params.toString();
  return request(`/repartidor/me/ruta${query ? `?${query}` : ""}`);
}

/** Entregas finalizadas del repartidor autenticado. */
export function getMiHistorialRepartidor() {
  return request("/repartidor/me/historial");
}

/** Pedidos listos para retirar que puede tomar el repartidor autenticado. */
export function getMisPedidosDisponibles() {
  return request("/repartidor/me/pedidos-disponibles");
}

/** Toma un pedido disponible para el repartidor autenticado. */
export function tomarPedidoDisponible(pedidoId) {
  return request(`/repartidor/me/pedidos/${pedidoId}/tomar`, {
    method: "PATCH",
  });
}

/** Confirma el retiro del pedido asignado al repartidor autenticado. */
export function retirarPedidoAsignado(pedidoId) {
  return request(`/repartidor/me/pedidos/${pedidoId}/retirar`, {
    method: "PATCH",
  });
}

/** Inicia el viaje de un pedido retirado por el repartidor autenticado. */
export function iniciarViajePedido(pedidoId) {
  return request(`/repartidor/me/pedidos/${pedidoId}/iniciar-viaje`, {
    method: "PATCH",
  });
}

/** Confirma la entrega del pedido propio en camino. */
export function entregarPedidoActual(pedidoId) {
  return request(`/repartidor/me/pedidos/${pedidoId}/entregar`, {
    method: "PATCH",
  });
}
