import { request } from "./httpClient";

/** Perfil del repartidor autenticado (rol REPARTIDOR), con su pedido actual. */
export function getMiPerfilRepartidor() {
  return request("/repartidor/me");
}

/** Entregas finalizadas del repartidor autenticado. */
export function getMiHistorialRepartidor() {
  return request("/repartidor/me/historial");
}
