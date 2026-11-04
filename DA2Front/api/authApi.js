const AUTH_API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8081") + "/auth";

export async function loginRequest({ email, password }) {
  let response;
  try {
    response = await fetch(`${AUTH_API_BASE_URL}/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });
  } catch {
    throw new Error(
      "No se pudo conectar con el servidor. Verificá que el backend esté corriendo en localhost:8081."
    );
  }

  if (!response.ok) {
    let errorMsg = "No se pudo iniciar sesión. Intentá nuevamente en unos minutos.";
    try {
      const errorData = await response.json();
      if (errorData && errorData.message) {
        errorMsg = errorData.message;
      }
    } catch {
      if (response.status === 401 || response.status === 403) {
        errorMsg = "Correo o contraseña incorrectos.";
      }
    }
    throw new Error(errorMsg);
  }

  return response.json();
}

/**
 * SEC-W03: Renueva el access token enviando el refresh token vigente.
 * Lanza error (401) si el refresh token fue revocado (logout) o venció.
 */
export async function refreshTokenRequest(refreshToken) {
  const response = await fetch(`${AUTH_API_BASE_URL}/refresh`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken }),
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    const error = new Error(errorBody?.message || "Sesión expirada o revocada.");
    error.status = response.status;
    throw error;
  }

  return response.json();
}

/**
 * SEC-W03: Cierra la sesión en el servidor revocando el refresh token.
 */
export async function logoutRequest(refreshToken) {
  if (!refreshToken) return;
  try {
    await fetch(`${AUTH_API_BASE_URL}/logout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
  } catch {
    // Incluso si el servidor no responde, el cliente limpia su sesión local.
  }
}

/**
 * Registro de usuario nuevo.
 *
 * El backend espera un RegisterDTO polimórfico (discriminado por "rol"):
 *   Base:       { email, password, nombre, apellido, dni, telefono, rol }
 *   COMERCIO:   + { nombreComercial, razonSocial, cuit, direccion }
 *   REPARTIDOR: + { vehiculo }
 *
 * El rol DEPOSITO ya no se registra acá: un COMERCIO lo agrega con
 * POST /depositos (ver api/depositosApi.js).
 *
 * useRegister.js arma el objeto plano combinando los datos base con los
 * específicos del rol elegido; acá simplemente se reenvía tal cual.
 */
export async function registerRequest(payload) {
  let response;
  try {
    response = await fetch(`${AUTH_API_BASE_URL}/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error(
      "No se pudo conectar con el servidor. Verificá que el backend esté corriendo en localhost:8081."
    );
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    if (response.status === 409 || response.status === 400) {
      throw new Error(
        errorBody?.message || "El correo ya está registrado o los datos son inválidos."
      );
    }
    throw new Error("No se pudo completar el registro. Intentá nuevamente.");
  }

  return response.json();
}
