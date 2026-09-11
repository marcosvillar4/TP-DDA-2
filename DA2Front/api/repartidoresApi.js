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

export function getRepartidores() {
  return request("/repartidores");
}

export function getRepartidorPorId(id) {
  return request(`/repartidores/${id}`);
}

export function crearRepartidor(repartidor) {
  return request("/repartidores", {
    method: "POST",
    body: JSON.stringify(repartidor),
  });
}

export function actualizarRepartidor(id, repartidor) {
  return request(`/repartidores/${id}`, {
    method: "PUT",
    body: JSON.stringify(repartidor),
  });
}

export function cambiarEstadoRepartidor(id, estado) {
  return request(`/repartidores/${id}/estado`, {
    method: "PATCH",
    body: JSON.stringify({ estado }),
  });
}

export function activarRepartidor(id) {
  return request(`/repartidores/${id}/activar`, {
    method: "PATCH",
  });
}

export function desactivarRepartidor(id) {
  return request(`/repartidores/${id}/desactivar`, {
    method: "PATCH",
  });
}

export function asignarPedido(repartidorId, pedidoId) {
  return request(`/repartidores/${repartidorId}/pedidos/${pedidoId}/asignar`, {
    method: "POST",
  });
}

export function getHistorialRepartidor(id) {
  return request(`/repartidores/${id}/historial`);
}

export function getPedidosAsignables() {
  return request("/repartidores/pedidos/asignables");
}

export function getUsuariosRepartidoresDisponibles() {
  return request("/repartidores/usuarios-disponibles");
}
