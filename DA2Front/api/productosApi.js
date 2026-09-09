const API_BASE_URL = "http://localhost:8081";

function getAuthHeaders() {
  const token = localStorage.getItem("logired_token");

  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
}

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      searchParams.append(key, value);
    }
  });

  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

async function request(path, options = {}) {
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

  const text = await response.text();
  let body = null;

  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = { message: text };
    }
  }

  if (!response.ok) {
    const error = new Error(
      body?.message || "Ocurrió un error inesperado. Intentá nuevamente."
    );
    error.status = response.status;
    error.body = body;
    throw error;
  }

  return body;
}

export function getComercios() {
  return request("/comercios");
}

export function getProductos(filtros = {}) {
  return request(`/productos${buildQuery(filtros)}`);
}

export function getProductoPorId(id) {
  return request(`/productos/${id}`);
}

export function crearProducto(producto) {
  return request("/productos", {
    method: "POST",
    body: JSON.stringify(producto),
  });
}

export function actualizarProducto(id, producto) {
  return request(`/productos/${id}`, {
    method: "PUT",
    body: JSON.stringify(producto),
  });
}

export function activarProducto(id) {
  return request(`/productos/${id}/activar`, {
    method: "PATCH",
  });
}

export function desactivarProducto(id) {
  return request(`/productos/${id}/desactivar`, {
    method: "PATCH",
  });
}
