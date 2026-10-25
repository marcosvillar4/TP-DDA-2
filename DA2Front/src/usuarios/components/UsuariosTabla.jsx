import { useNavigate } from "react-router-dom";
import { ArrowRight, ChevronRight, Inbox } from "lucide-react";
import { EstadoBadge, RolBadge } from "./UsuarioBadge";
import { iniciales } from "../utils/usuarioLabels";

const FILAS_SKELETON = 6;

function FilaSkeleton() {
  return (
    <tr>
      <td>
        <div className="usuario-nombre-cell">
          <span className="skeleton skeleton-avatar" />
          <div className="skeleton-columna">
            <span className="skeleton skeleton-linea" style={{ width: 150 }} />
            <span className="skeleton skeleton-linea" style={{ width: 200 }} />
          </div>
        </div>
      </td>
      <td><span className="skeleton skeleton-linea" style={{ width: 80 }} /></td>
      <td><span className="skeleton skeleton-linea" style={{ width: 90 }} /></td>
      <td><span className="skeleton skeleton-linea" style={{ width: 70, marginLeft: "auto" }} /></td>
    </tr>
  );
}

export function UsuariosTabla({ usuarios, loading, cola, hayFiltros, onLimpiar }) {
  const navigate = useNavigate();

  // Si el usuario está pendiente, se le pasa la cola para poder revisar de a uno.
  const abrir = (u) =>
    navigate(`/usuarios/${u.id}`, {
      state: u.estado === "EN_EVALUACION" ? { cola } : undefined,
    });

  return (
    <div className="usuarios-tabla-scroll">
      <table className="usuarios-tabla">
        <thead>
          <tr>
            <th>Usuario</th>
            <th>Rol</th>
            <th>Estado</th>
            <th className="usuarios-col-accion"><span className="sr-only">Acciones</span></th>
          </tr>
        </thead>
        <tbody>
          {loading &&
            Array.from({ length: FILAS_SKELETON }, (_, i) => <FilaSkeleton key={i} />)}

          {!loading &&
            usuarios.map((u) => {
              const pendiente = u.estado === "EN_EVALUACION";
              return (
                <tr
                  key={u.id}
                  className={`fila-clicable${pendiente ? " fila-pendiente" : ""}`}
                  onClick={() => abrir(u)}
                >
                  <td>
                    <div className="usuario-nombre-cell">
                      <span className="usuario-avatar">{iniciales(u)}</span>
                      <div className="usuario-nombre-texto">
                        <span className="usuario-nombre-principal">{u.nombre} {u.apellido}</span>
                        <span className="usuario-email-chico">{u.email}</span>
                      </div>
                    </div>
                  </td>
                  <td><RolBadge rol={u.rol} /></td>
                  <td><EstadoBadge estado={u.estado} /></td>
                  <td className="usuarios-col-accion">
                    <button
                      type="button"
                      className={pendiente ? "btn-revisar" : "btn-ver"}
                      onClick={(e) => {
                        e.stopPropagation();
                        abrir(u);
                      }}
                    >
                      {pendiente ? <>Revisar <ArrowRight size={14} /></> : <>Ver <ChevronRight size={14} /></>}
                    </button>
                  </td>
                </tr>
              );
            })}

          {!loading && usuarios.length === 0 && (
            <tr>
              <td colSpan={4}>
                <div className="usuarios-vacio">
                  <span className="usuarios-vacio-icono"><Inbox size={22} /></span>
                  <h4>{hayFiltros ? "Sin resultados" : "Todavía no hay usuarios"}</h4>
                  <p>
                    {hayFiltros
                      ? "Ningún usuario coincide con los filtros aplicados."
                      : "Cuando alguien se registre, va a aparecer acá."}
                  </p>
                  {hayFiltros && (
                    <button type="button" className="u-btn u-btn-ghost" onClick={onLimpiar}>
                      Limpiar filtros
                    </button>
                  )}
                </div>
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}