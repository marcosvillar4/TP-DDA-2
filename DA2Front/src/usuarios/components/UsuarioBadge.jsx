import { ESTADO_LABELS, ESTADO_CLASES } from "../utils/usuarioLabels";
import { ROLE_LABELS } from "../../layout/navConfig";

export function EstadoBadge({ estado }) {
  return <span className={`badge ${ESTADO_CLASES[estado]}`}>{ESTADO_LABELS[estado] || estado}</span>;
}

export function RolBadge({ rol }) {
  return <span className="badge badge-navy">{ROLE_LABELS[rol] || rol}</span>;
}