import { Link } from "react-router-dom";
import { Warehouse, ArrowRight, ChevronRight } from "lucide-react";
import { Panel } from "../Panel";
import { EstadoBadge } from "../UsuarioBadge";

/**
 * Depósitos del comercio. Cada uno lleva al detalle de su usuario, donde el admin lo valida:
 * los depósitos los carga el comercio y quedan EN_EVALUACION hasta que alguien los aprueba.
 */
export function ComercioDepositosPanel({ perfil }) {
  const depositos = perfil.depositos ?? [];
  const cupo = perfil.maxDepositos ? ` de ${perfil.maxDepositos}` : "";

  return (
    <Panel titulo={`Depósitos (${depositos.length}${cupo})`} icono={Warehouse}>
      {depositos.length === 0 ? (
        <p className="detalle-panel-vacio">Este comercio todavía no cargó depósitos.</p>
      ) : (
        <ul className="depositos-lista">
          {depositos.map((d) => {
            const pendiente = d.usuarioEstado === "EN_EVALUACION";
            return (
              <li key={d.id} className="deposito-fila">
                <span className="deposito-icono"><Warehouse size={16} /></span>
                <div className="deposito-info">
                  <span className="deposito-nombre">{d.nombre}</span>
                  <span className="deposito-detalle">
                    {d.direccion}
                    {d.responsable && ` · ${d.responsable}`}
                  </span>
                </div>
                {d.usuarioEstado && <EstadoBadge estado={d.usuarioEstado} />}
                {d.usuarioId && (
                  <Link to={`/usuarios/${d.usuarioId}`} className={pendiente ? "btn-revisar" : "btn-ver"}>
                    {pendiente ? <>Revisar <ArrowRight size={14} /></> : <>Ver <ChevronRight size={14} /></>}
                  </Link>
                )}
              </li>
            );
          })}
        </ul>
      )}
    </Panel>
  );
}