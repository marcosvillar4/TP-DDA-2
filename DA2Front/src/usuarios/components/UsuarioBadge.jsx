import { ESTADO_LABELS, ESTADO_CLASES } from "../utils/usuarioLabels";
import { ROLE_LABELS } from "../../layout/navConfig";

export function EstadoBadge({ estado }) {
  return (
    <span className={`badge ${ESTADO_CLASES[estado] ?? "badge-slate"}`}>
      <span className="badge-dot" aria-hidden="true" />
      {ESTADO_LABELS[estado] || estado}
    </span>
  );
}

export function RolBadge({ rol }) {
  return <span className="badge badge-navy">{ROLE_LABELS[rol] || rol}</span>;
}