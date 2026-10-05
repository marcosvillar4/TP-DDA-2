/**
 * El comercio no tiene un estado propio en el backend: se usa el estado de la
 * cuenta del usuario dueño (EstadoUsuario), que administra el ADMIN desde
 * el módulo Usuarios.
 */
export const ESTADO_COMERCIO_LABELS = {
  VALIDADO: "Activo",
  EN_EVALUACION: "En evaluación",
  RECHAZADO: "Rechazado",
  BLOQUEADO: "Bloqueado",
};

export function labelEstadoComercio(estado) {
  return ESTADO_COMERCIO_LABELS[estado] ?? "Sin datos";
}

export function esComercioActivo(estado) {
  return estado === "VALIDADO";
}
