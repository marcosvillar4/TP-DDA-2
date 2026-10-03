import { request } from "./httpClient";

export function getDepositosPorComercio(comercioId) {
  return request(`/depositos/comercio/${comercioId}`);
}

/**
 * Un COMERCIO agrega un depósito. El backend lo vincula automáticamente al
 * comercio del usuario autenticado (no se envía comercioId).
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
