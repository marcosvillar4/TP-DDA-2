import { request } from "./httpClient";

export function getNotificaciones() {
  return request("/notificaciones");
}

export async function getCantidadNotificacionesNoLeidas() {
  const respuesta = await request("/notificaciones/no-leidas");
  return respuesta?.cantidad ?? 0;
}

export function marcarNotificacionLeida(id) {
  return request(`/notificaciones/${id}/leida`, { method: "PATCH" });
}
