import { request } from "./httpClient";

export function getDepositos() {
  return request("/depositos");
}

export function getDepositoPorId(id) {
  return request(`/depositos/${id}`);
}

export function updateDeposito(id, datos) {
  return request(`/depositos/${id}`, {
    method: "PUT",
    body: JSON.stringify(datos),
  });
}

export function getComercios() {
  return request("/comercios");
}

export function getItemsInventarioPorDeposito(depositoId) {
  return request(`/items-inventario/deposito/${depositoId}`);
}

// ── Depósitos de un comercio (rol COMERCIO / DEPOSITO) ──

export function getDepositosPorComercio(comercioId) {
  return request(`/depositos/comercio/${comercioId}`);
}

/** Depósito del usuario autenticado (rol DEPOSITO). */
export function getMiDeposito() {
  return request("/depositos/me");
}

/**
 * Un COMERCIO agrega un depósito. El backend lo vincula automáticamente al
 * comercio del usuario autenticado (no se envía comercioId) y rechaza el alta
 * si el comercio ya tiene el máximo de depósitos.
 *
 * Body (RegistroDepositoDTO):
 *   { email, password, nombre, apellido, dni, telefono,
 *     nombreDeposito, direccionDeposito }
 */
export function agregarDeposito(deposito) {
  return request("/depositos", {
    method: "POST",
    body: JSON.stringify(deposito),
  });
}
