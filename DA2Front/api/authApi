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