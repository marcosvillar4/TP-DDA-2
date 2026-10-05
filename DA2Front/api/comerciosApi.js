import { request } from "./httpClient";

export function getComercios() {
  return request("/comercios");
}

export function getComercioPorId(id) {
  return request(`/comercios/${id}`);
}

/** Comercio del usuario autenticado (rol COMERCIO). */
export function getMiComercio() {
  return request("/comercios/me");
}

/**
 * Actualiza los datos del comercio.
 * Body esperado por el backend (ComercioCreateDTO):
 *   { nombreComercial, razonSocial, direccion, cuit, telefono, email }
 */
export function actualizarComercio(id, comercio) {
  return request(`/comercios/${id}`, {
    method: "PUT",
    body: JSON.stringify(comercio),
  });
}
