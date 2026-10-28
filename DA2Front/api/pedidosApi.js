import { request } from "./httpClient";

export function getPedidos() {
  return request("/api/pedidos");
}

export function getPedidoById(id) {
  return request(`/api/pedidos/${id}`);
}

export function getPedidosPorComercio(comercioId) {
  return request(`/api/pedidos/comercio/${comercioId}`);
}

/** Cotiza el envio (distancia + cantidad de objetos) sin crear el pedido. */
export function cotizarPedido(data) {
  return request("/api/pedidos/cotizar", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

/** Recorrido retiro -> entrega del pedido (paradas, distancia, tiempo y polilinea). */
export function getRutaPedido(id) {
  return request(`/api/pedidos/${id}/ruta`);
}

export function crearPedido(data) {
  return request("/api/pedidos", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export function asignarRepartidor(pedidoId, repartidorId) {
  return request(`/api/pedidos/${pedidoId}/asignar/${repartidorId}`, {
    method: "PATCH",
  });
}

export function iniciarViajePedido(pedidoId) {
  return request(`/api/pedidos/${pedidoId}/iniciar-viaje`, {
    method: "PATCH",
  });
}

export function entregarPedido(pedidoId) {
  return request(`/api/pedidos/${pedidoId}/entregar`, {
    method: "PATCH",
  });
}

export function cancelarPedido(pedidoId) {
  return request(`/api/pedidos/${pedidoId}/cancelar`, {
    method: "DELETE",
  });
}

export function marcarListoParaRetirar(pedidoId) {
  return request(`/api/pedidos/${pedidoId}/listo-para-retirar`, {
    method: "PATCH",
  });
}

export function marcarRetirado(pedidoId) {
  return request(`/api/pedidos/${pedidoId}/retirar`, {
    method: "PATCH",
  });
}
