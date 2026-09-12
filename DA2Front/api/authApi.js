const AUTH_API_BASE_URL = "http://localhost:8081/auth";

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
    if (response.status === 401 || response.status === 403) {
      throw new Error("Correo o contraseña incorrectos.");
    }
    throw new Error("No se pudo iniciar sesión. Intentá nuevamente en unos minutos.");
  }

  return response.json();
}

/**
 * Registro de usuario nuevo.
 *
 * El backend espera un RegisterDTO polimórfico (discriminado por "rol"):
 *   Base:       { email, password, nombre, apellido, dni, telefono, rol }
 *   COMERCIO:   + { nombreComercial, razonSocial, cuit, direccion }
 *   DEPOSITO:   + { nombreDeposito, direccionDeposito }
 *   REPARTIDOR: + { vehiculo }
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