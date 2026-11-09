import { request } from "./httpClient";

/* ───────── Comercio ───────── */

/** Pagos pendientes del comercio autenticado. */
export function getPagosPendientes() {
  return request("/api/pagos/pendientes");
}

/** Todos los pagos del comercio autenticado (opcionalmente filtrados por estado). */
export function getPagosComercio(estado) {
  const query = estado ? `?estado=${encodeURIComponent(estado)}` : "";
  return request(`/api/pagos${query}`);
}

/** Paga (simulado) el envio de un pedido. */
export function pagarPago(pagoId, datos) {
  return request(`/api/pagos/${pagoId}/pagar`, {
    method: "POST",
    body: JSON.stringify(datos),
  });
}

/* ───────── Repartidor ───────── */

/** Cobros del repartidor autenticado: recibidos y pendientes (estado opcional). */
export function getMisCobros(estado) {
  const query = estado ? `?estado=${encodeURIComponent(estado)}` : "";
  return request(`/repartidor/me/pagos${query}`);
}
