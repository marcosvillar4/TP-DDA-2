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
