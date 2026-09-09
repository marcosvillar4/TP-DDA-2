import { Search } from "lucide-react";
import "../styles/ProductoFilters.css";

export function ProductoFilters({
  filtros,
  comercios,
  categorias,
  onChange,
  onClear,
}) {
  const hasFilters =
    filtros.buscar || filtros.comercioId || filtros.categoria || filtros.estado;

  return (
    <div className="producto-filters">
      <div className="producto-search-wrapper">
        <Search className="producto-search-icon" size={18} />
        <input
          type="text"
          placeholder="Nombre o SKU..."
          className="producto-search-input"
          value={filtros.buscar}
          onChange={(e) => onChange("buscar", e.target.value)}
        />
      </div>

      <select
        className="producto-filter-select"
        value={filtros.comercioId}
        onChange={(e) => onChange("comercioId", e.target.value)}
      >
        <option value="">Todos los comercios</option>
        {comercios.map((comercio) => (
          <option key={comercio.id} value={comercio.id}>
            {comercio.nombre}
          </option>
        ))}
      </select>

      <select
        className="producto-filter-select"
        value={filtros.categoria}
        onChange={(e) => onChange("categoria", e.target.value)}
      >
        <option value="">Todas las categorías</option>
        {categorias.map((categoria) => (
          <option key={categoria} value={categoria}>
            {categoria}
          </option>
        ))}
      </select>

      <select
        className="producto-filter-select"
        value={filtros.estado}
        onChange={(e) => onChange("estado", e.target.value)}
      >
        <option value="">Todos los estados</option>
        <option value="ACTIVO">Activos</option>
        <option value="INACTIVO">Inactivos</option>
      </select>

      {hasFilters && (
        <button
          type="button"
          className="producto-filter-clear"
          onClick={onClear}
        >
          Limpiar filtros
        </button>
      )}
    </div>
  );
}
