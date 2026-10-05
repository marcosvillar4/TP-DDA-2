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