import { request } from "./httpClient";

export function getTransportistas() {
  return request("/transportistas");
}

export function crearTransportista(transportista) {
  return request("/transportistas", {
    method: "POST",
    body: JSON.stringify(transportista),
  });
}

export function actualizarTransportista(id, transportista) {
  return request(`/transportistas/${id}`, {
    method: "PUT",
    body: JSON.stringify(transportista),
  });
}

export function cambiarEstadoTransportista(id, estado) {
  return request(`/transportistas/${id}/estado`, {
    method: "PATCH",
    body: JSON.stringify({ estado }),
  });
}

export function activarTransportista(id) {
  return request(`/transportistas/${id}/activar`, { method: "PATCH" });
}

export function desactivarTransportista(id) {
  return request(`/transportistas/${id}/desactivar`, { method: "PATCH" });
}
