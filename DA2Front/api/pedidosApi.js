import { request } from "./httpClient";

export function getPedidos() {
  return request("/api/pedidos");
}

export function getPedidosPorComercio(comercioId) {
  return request(`/api/pedidos/comercio/${comercioId}`);
}