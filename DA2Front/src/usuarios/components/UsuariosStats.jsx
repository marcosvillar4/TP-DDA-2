import { Users, Clock, UserCheck, Ban, UserX } from "lucide-react";

const TARJETAS = [
  { clave: "TODOS", label: "Total de usuarios", valor: "total", icono: Users, tono: "navy" },
  { clave: "EN_EVALUACION", label: "En evaluación", valor: "enEvaluacion", icono: Clock, tono: "amber" },
  { clave: "VALIDADO", label: "Validados", valor: "validados", icono: UserCheck, tono: "green" },
  { clave: "BLOQUEADO", label: "Bloqueados", valor: "bloqueados", icono: Ban, tono: "red" },
  { clave: "RECHAZADO", label: "Rechazados", valor: "rechazados", icono: UserX, tono: "slate" },
];

/** Las tarjetas son el filtro por estado: un click filtra, otro click sobre la misma lo quita. */
export function UsuariosStats({ stats, loading, estadoActivo, onSelect }) {
  return (
    <div className="usuarios-stats" role="group" aria-label="Filtrar por estado">
      {TARJETAS.map(({ clave, label, valor, icono: Icono, tono }) => {
        const cantidad = stats[valor];
        const alerta = clave === "EN_EVALUACION" && cantidad > 0 && !loading;
        const activo = estadoActivo === clave;

        return (
          <button
            key={clave}
            type="button"
            aria-pressed={activo}
            className={`stat-card stat-tono-${tono}${activo ? " activo" : ""}${alerta ? " alerta" : ""}`}
            onClick={() => onSelect(clave)}
          >
            {alerta && <span className="stat-alerta-dot" aria-hidden="true" />}
            <span className="stat-icono">
              <Icono size={18} />
            </span>
            <span className="stat-numero">{loading ? "–" : cantidad}</span>
            <span className="stat-label">{label}</span>
            {alerta && <span className="stat-hint">Revisar ahora →</span>}
          </button>
        );
      })}
    </div>
  );
}