import { Search, X } from "lucide-react";
import { ROLE_LABELS } from "../../layout/navConfig";

export function UsuariosFiltros({
  busqueda, setBusqueda,
  filtroRol, setFiltroRol,
  hayFiltros, onLimpiar,
}) {
  return (
    <div className="usuarios-filtros">
      <div className="usuarios-buscador-wrap">
        <Search className="usuarios-buscador-icono" size={16} />
        <input
          type="search"
          placeholder="Buscar por nombre o email..."
          value={busqueda}
          onChange={(e) => setBusqueda(e.target.value)}
          className="usuarios-buscador"
          aria-label="Buscar usuarios"
        />
      </div>

      <select
        value={filtroRol}
        onChange={(e) => setFiltroRol(e.target.value)}
        aria-label="Filtrar por rol"
      >
        <option value="TODOS">Todos los roles</option>
        {Object.entries(ROLE_LABELS).map(([valor, label]) => (
          <option key={valor} value={valor}>{label}</option>
        ))}
      </select>

      {hayFiltros && (
        <button type="button" className="u-btn u-btn-ghost" onClick={onLimpiar}>
          <X size={15} /> Limpiar filtros
        </button>
      )}
    </div>
  );
}