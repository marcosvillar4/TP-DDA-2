const API_BASE_URL = "http://localhost:8081";

function getAuthHeaders() {
  const token = localStorage.getItem("logired_token");
  return {
    "Content-Type": "application/json",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };
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

export function getComercios() {
  return request("/comercios");
}

export function getProductos() {
  return request("/productos");
}

export function getDepositos() {
  return request("/deposito");
}

export function getDepositosPorComercio(comercioId) {
  return request(`/deposito/comercio/${comercioId}`);
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

export function actualizarCantidadItem(id, cantidad) {
  return request(`/items-inventario/${id}/cantidad?cantidad=${cantidad}`, {
    method: "PUT",
  });
}

export function eliminarItemInventario(id) {
  return request(`/items-inventario/${id}`, { method: "DELETE" });
}