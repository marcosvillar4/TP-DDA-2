import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Eye, Plus, Search, Users, CheckCircle2, Truck, PauseCircle } from "lucide-react";
import { useRepartidores } from "../../hooks/useRepartidores";
import RepartidorEstadoBadge from "./components/RepartidorEstadoBadge";
import RepartidorFormModal from "./components/RepartidorFormModal";
import "./styles/RepartidoresPage.css";

export default function RepartidoresPage() {
  const navigate = useNavigate();
  const {
    repartidoresFiltrados,
    usuariosDisponibles,
    loading,
    saving,
    error,
    filters,
    zonas,
    kpis,
    updateFilter,
    resetFilters,
    guardarRepartidor,
  } = useRepartidores();

  const [isCreateOpen, setIsCreateOpen] = useState(false);

  async function handleCreate(payload) {
    const ok = await guardarRepartidor(payload);
    if (ok) {
      setIsCreateOpen(false);
    }
  }

  const hasFilters = filters.buscar || filters.estado || filters.zona;

  return (
    <div className="repartidores-page">
      <div className="repartidores-header">
        <div>
          <h1 className="repartidores-title">Repartidores</h1>
          <p className="repartidores-subtitle">
            {repartidoresFiltrados.length} repartidor{repartidoresFiltrados.length !== 1 ? "es" : ""} mostrado{repartidoresFiltrados.length !== 1 ? "s" : ""}
          </p>
        </div>
        <button className="repartidores-btn-primary" onClick={() => setIsCreateOpen(true)}>
          <Plus size={16} />
          Nuevo repartidor
        </button>
      </div>

      <div className="repartidores-kpi-grid">
        <Kpi icon={Users} label="Total repartidores" value={kpis.total} tone="blue" />
        <Kpi icon={CheckCircle2} label="Disponibles" value={kpis.disponibles} tone="green" />
        <Kpi icon={Truck} label="En entrega" value={kpis.enEntrega} tone="violet" />
        <Kpi icon={PauseCircle} label="No disponibles" value={kpis.noDisponibles} tone="amber" />
      </div>

      <div className="repartidores-filters">
        <div className="repartidores-search-wrapper">
          <Search className="repartidores-search-icon" size={18} />
          <input
            type="text"
            placeholder="Nombre, patente o vehículo..."
            className="repartidores-search-input"
            value={filters.buscar}
            onChange={(event) => updateFilter("buscar", event.target.value)}
          />
        </div>
        <select
          className="repartidores-select"
          value={filters.estado}
          onChange={(event) => updateFilter("estado", event.target.value)}
        >
          <option value="">Todos los estados</option>
          <option value="DISPONIBLE">Disponibles</option>
          <option value="EN_ENTREGA">En entrega</option>
          <option value="NO_DISPONIBLE">No disponibles</option>
        </select>
        <select
          className="repartidores-select"
          value={filters.zona}
          onChange={(event) => updateFilter("zona", event.target.value)}
        >
          <option value="">Todas las zonas</option>
          {zonas.map((zona) => (
            <option key={zona} value={zona}>
              {zona}
            </option>
          ))}
        </select>
        {hasFilters && (
          <button className="repartidores-btn-clear" onClick={resetFilters}>
            Limpiar filtros
          </button>
        )}
      </div>

      <div className="repartidores-table-card">
        {loading ? (
          <div className="repartidores-state">Cargando repartidores...</div>
        ) : error ? (
          <div className="repartidores-state error">{error}</div>
        ) : (
          <div className="repartidores-table-responsive">
            <table className="repartidores-table">
              <thead>
                <tr>
                  <th>Repartidor</th>
                  <th>Teléfono</th>
                  <th>Vehículo</th>
                  <th>Patente</th>
                  <th>Estado</th>
                  <th>Pedido actual</th>
                  <th>Zona</th>
                  <th>Ver</th>
                </tr>
              </thead>
              <tbody>
                {repartidoresFiltrados.map((repartidor) => (
                  <tr key={repartidor.id}>
                    <td className="repartidores-driver-cell">
                      <span className="repartidores-avatar">{getInitials(repartidor.nombreCompleto)}</span>
                      <span>
                        <strong>{repartidor.nombreCompleto}</strong>
                        <small>{repartidor.email}</small>
                      </span>
                    </td>
                    <td>{repartidor.telefono}</td>
                    <td>{repartidor.tipoVehiculo}</td>
                    <td className="repartidores-patente">{repartidor.patente}</td>
                    <td>
                      <RepartidorEstadoBadge estado={repartidor.estado} activo={repartidor.activo} />
                    </td>
                    <td className="repartidores-pedido">
                      {repartidor.pedidoActual ? `#${repartidor.pedidoActual.id}` : "—"}
                    </td>
                    <td>{repartidor.zona}</td>
                    <td>
                      <button
                        className="repartidores-btn-view"
                        onClick={() => navigate(`/repartidores/${repartidor.id}`)}
                      >
                        <Eye size={16} />
                        Ver
                      </button>
                    </td>
                  </tr>
                ))}
                {repartidoresFiltrados.length === 0 && (
                  <tr>
                    <td className="repartidores-empty" colSpan="8">
                      No se encontraron repartidores con los filtros aplicados.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <RepartidorFormModal
        open={isCreateOpen}
        mode="create"
        usuariosDisponibles={usuariosDisponibles}
        saving={saving}
        onClose={() => setIsCreateOpen(false)}
        onSubmit={handleCreate}
      />
    </div>
  );
}

function Kpi({ icon: Icon, label, value, tone }) {
  return (
    <div className={`repartidores-kpi ${tone}`}>
      <span className="repartidores-kpi-icon">
        <Icon size={18} />
      </span>
      <span className="repartidores-kpi-value">{value}</span>
      <span className="repartidores-kpi-label">{label}</span>
    </div>
  );
}

function getInitials(name = "") {
  return name
    .split(" ")
    .filter(Boolean)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}
