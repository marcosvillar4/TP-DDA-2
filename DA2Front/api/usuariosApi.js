import { request } from "./httpClient";

export function getUsuariosAdmin() {
  return request("/usuarios/admin/all");
}