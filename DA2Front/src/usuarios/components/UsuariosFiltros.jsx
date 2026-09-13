import { ROLE_LABELS } from "../../layout/navConfig";
import { ESTADO_LABELS } from "../utils/usuarioLabels";

export function UsuariosFiltros({
  busqueda, setBusqueda,
  filtroRol, setFiltroRol,
  filtroEstado, setFiltroEstado,
}) {
  return (
    <div className="usuarios-filtros">
      <input
        type="text"
        placeholder="Buscar por nombre o email..."
        value={busqueda}
        onChange={(e) => setBusqueda(e.target.value)}
        className="usuarios-buscador"
      />
      <select value={filtroRol} onChange={(e) => setFiltroRol(e.target.value)}>
        <option value="TODOS">Todos los roles</option>
        {Object.entries(ROLE_LABELS).map(([valor, label]) => (
          <option key={valor} value={valor}>{label}</option>
        ))}
      </select>
      <select value={filtroEstado} onChange={(e) => setFiltroEstado(e.target.value)}>
        <option value="TODOS">Todos los estados</option>
        {Object.entries(ESTADO_LABELS).map(([valor, label]) => (
          <option key={valor} value={valor}>{label}</option>
        ))}
      </select>
    </div>
  );
}