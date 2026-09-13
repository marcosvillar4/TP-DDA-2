import { request } from "./httpClient";

export function getRepartidores() {
  return request("/repartidores");
}

export function getRepartidorPorId(id) {
  return request(`/repartidores/${id}`);
}

export function crearRepartidor(repartidor) {
  return request("/repartidores", {
    method: "POST",
    body: JSON.stringify(repartidor),
  });
}

export function actualizarRepartidor(id, repartidor) {
  return request(`/repartidores/${id}`, {
    method: "PUT",
    body: JSON.stringify(repartidor),
  });
}

export function cambiarEstadoRepartidor(id, estado) {
  return request(`/repartidores/${id}/estado`, {
    method: "PATCH",
    body: JSON.stringify({ estado }),
  });
}

export function activarRepartidor(id) {
  return request(`/repartidores/${id}/activar`, {
    method: "PATCH",
  });
}

export function desactivarRepartidor(id) {
  return request(`/repartidores/${id}/desactivar`, {
    method: "PATCH",
  });
}

export function asignarPedido(repartidorId, pedidoId) {
  return request(`/repartidores/${repartidorId}/pedidos/${pedidoId}/asignar`, {
    method: "POST",
  });
}

export function getHistorialRepartidor(id) {
  return request(`/repartidores/${id}/historial`);
}

export function getPedidosAsignables() {
  return request("/repartidores/pedidos/asignables");
}

export function getUsuariosRepartidoresDisponibles() {
  return request("/repartidores/usuarios-disponibles");
}
