const API_BASE_URL = "http://localhost:8081";

function getAuthHeaders() {
  const token = localStorage.getItem("logired_token");
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

export async function request(path, options = {}) {
  let response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        ...getAuthHeaders(),
        ...(options.headers || {}),
      },
    });
  } catch {
    throw new Error(
      "No se pudo conectar con el servidor. Verificá que el backend esté corriendo en localhost:8081."
    );
  }

  if (!response.ok) {
    let message = "Ocurrió un error inesperado. Intentá nuevamente.";
    try {
      const body = await response.json();
      if (body?.message) message = body.message;
    } catch {
      // La respuesta no trae un cuerpo JSON con detalle del error.
    }
    throw new Error(message);
  }

  const text = await response.text();
  return text ? JSON.parse(text) : null;
}