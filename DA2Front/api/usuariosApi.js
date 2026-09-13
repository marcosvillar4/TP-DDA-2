import { request } from "./httpClient";
const API_BASE_URL = "http://localhost:8081/usuarios";

export function getUsuariosAdmin() {
  return request("/usuarios/admin/all");
}

function getAuthHeaders() {
  const token = localStorage.getItem("logired_token");
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

async function handleResponse(response, mensajeErrorGenerico) {
  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    throw new Error(errorBody?.message || mensajeErrorGenerico);
  }
  if (response.status === 204) return null;
  return response.json();
}

export async function fetchUsuarios() {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/all`, { headers: getAuthHeaders() });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  return handleResponse(response, "No se pudo obtener la lista de usuarios.");
}

export async function fetchUsuarioPorId(id) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/${id}`, { headers: getAuthHeaders() });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  return handleResponse(response, "No se pudo obtener el usuario.");
}

export async function ejecutarTransicion(id, accion) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/${id}/${accion}`, {
      method: "PATCH",
      headers: getAuthHeaders(),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  return handleResponse(response, `No se pudo completar la acción: ${accion}.`);
}

export const validarUsuario = (id) => ejecutarTransicion(id, "validar");
export const rechazarUsuario = (id) => ejecutarTransicion(id, "rechazar");
export const bloquearUsuario = (id) => ejecutarTransicion(id, "bloquear");
export const desbloquearUsuario = (id) => ejecutarTransicion(id, "desbloquear");

export async function actualizarUsuarioAdmin(id, dto) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/${id}`, {
      method: "PATCH",
      headers: getAuthHeaders(),
      body: JSON.stringify(dto),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  return handleResponse(response, "No se pudieron guardar los cambios.");
}

export async function resetearPasswordAdmin(id, dto) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}/admin/${id}/password`, {
      method: "PATCH",
      headers: getAuthHeaders(),
      body: JSON.stringify(dto),
    });
  } catch {
    throw new Error("No se pudo conectar con el servidor.");
  }
  return handleResponse(response, "No se pudo resetear la contraseña.");
}