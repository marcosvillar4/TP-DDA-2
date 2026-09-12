import { request } from "./httpClient";

export function getComercios() {
  return request("/comercios");
}

export function getProductos() {
  return request("/productos");
}

export function getDepositos() {
  return request("/depositos");
}

export function getDepositosPorComercio(comercioId) {
  return request(`/depositos/comercio/${comercioId}`);
}

export async function getInventarioPorComercio(comercioId) {
  try {
    return await request(`/inventarios/comercio/${comercioId}`);
  } catch {
    // El comercio todavía no tiene un inventario creado
    // (o el backend no pudo resolverlo). Se trata como "sin inventario".
    return null;
  }
}

export function crearInventario(comercioId) {
  return request("/inventarios", {
    method: "POST",
    body: JSON.stringify({ comercioId }),
  });
}

export function eliminarInventario(id) {
  return request(`/inventarios/${id}`, { method: "DELETE" });
}

export function getItemsPorInventario(inventarioId) {
  return request(`/items-inventario/inventario/${inventarioId}`);
}

export function crearItemInventario({
  inventarioId,
  productoId,
  depositoId,
  cantidad,
}) {
  return request("/items-inventario", {
    method: "POST",
    body: JSON.stringify({ inventarioId, productoId, depositoId, cantidad }),
  });
}

export function actualizarItemInventario(id, { productoId, depositoId, cantidad }) {
  return request(`/items-inventario/${id}`, {
    method: "PUT",
    body: JSON.stringify({ productoId, depositoId, cantidad }),
  });
}

export function eliminarItemInventario(id) {
  return request(`/items-inventario/${id}`, { method: "DELETE" });
}