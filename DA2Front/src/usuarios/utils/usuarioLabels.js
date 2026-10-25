export const ESTADO_LABELS = {
  EN_EVALUACION: "En evaluación",
  VALIDADO: "Validado",
  RECHAZADO: "Rechazado",
  BLOQUEADO: "Bloqueado",
};

export const ESTADO_CLASES = {
  EN_EVALUACION: "badge-amber",
  VALIDADO: "badge-green",
  RECHAZADO: "badge-red",
  BLOQUEADO: "badge-slate",
};

export const VEHICULO_LABELS = {
  FURGONES_Y_UTILITARIOS_LIVIANOS: "Furgones y utilitarios livianos",
  CAMIONES_RIGIDOS: "Camiones rígidos",
  CAMIONES_ARTICULADOS: "Camiones articulados",
};

export const ESTADO_REPARTIDOR_LABELS = {
  DISPONIBLE: "Disponible",
  EN_ENTREGA: "En entrega",
  NO_DISPONIBLE: "No disponible",
};

export const ESTADO_REPARTIDOR_CLASES = {
  DISPONIBLE: "badge-green",
  EN_ENTREGA: "badge-amber",
  NO_DISPONIBLE: "badge-slate",
};

export function iniciales(usuario) {
  const n = usuario?.nombre?.charAt(0) ?? "";
  const a = usuario?.apellido?.charAt(0) ?? "";
  return (n + a).toUpperCase() || "?";
}

/** 30123456789 -> 30-12345678-9 (si no tiene 11 dígitos, lo deja como vino). */
export function formatearCuit(cuit) {
  if (!cuit) return cuit;
  const d = String(cuit).replace(/\D/g, "");
  return d.length === 11 ? `${d.slice(0, 2)}-${d.slice(2, 10)}-${d.slice(10)}` : cuit;
}