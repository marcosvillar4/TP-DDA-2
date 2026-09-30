import { Building2, Eye, MapPin, Search, Warehouse } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { useDepositos } from "../../hooks/useDepositos";
import "./styles/DepositosPage.css";

export default function DepositosPage() {
  const navigate = useNavigate();
  const {
    depositos,
    depositosFiltrados,
    busqueda,
    loading,
    error,
    resumen,
    setBusqueda,
    refrescar,
  } = useDepositos();

  return (
    <section className="depositos-page">
      <div className="depositos-header">
        <div>
          <h1 className="depositos-title">Depósitos</h1>
          <p className="depositos-subtitle">
            Consultá los depósitos registrados y su comercio asociado
          </p>
        </div>
      </div>

      <div className="depositos-summary-grid">
        <SummaryCard
          icon={Warehouse}
          label="Depósitos registrados"
          value={resumen.total}
          tone="blue"
        />
        <SummaryCard
          icon={Building2}
          label="Con comercio asociado"
          value={resumen.asociados}
          tone="green"
        />
        <SummaryCard
          icon={MapPin}
          label="Sin comercio asociado"
          value={resumen.sinComercio}
          tone="amber"
        />
      </div>

      <div className="depositos-toolbar">
        <div className="depositos-search">
          <Search size={18} className="depositos-search-icon" />
          <input
            type="search"
            value={busqueda}
            onChange={(event) => setBusqueda(event.target.value)}
            placeholder="Buscar por nombre, dirección o comercio..."
            className="depositos-search-input"
          />
        </div>
      </div>

      <div className="depositos-list-card">
        {loading ? (
          <div className="depositos-state">Cargando depósitos...</div>
        ) : error ? (
          <div className="depositos-state depositos-state-error">
            <p>{error}</p>
            <button type="button" onClick={refrescar} className="depositos-retry-button">
              Reintentar
            </button>
          </div>
        ) : depositos.length === 0 ? (
          <div className="depositos-state">
            Todavía no hay depósitos registrados.
          </div>
        ) : depositosFiltrados.length === 0 ? (
          <div className="depositos-state">
            No se encontraron depósitos con la búsqueda aplicada.
          </div>
        ) : (
          <div className="depositos-grid">
            {depositosFiltrados.map((deposito) => (
              <article key={deposito.id} className="deposito-card">
                <div className="deposito-card-icon">
                  <Warehouse size={22} />
                </div>

                <div className="deposito-card-content">
                  <h2>{deposito.nombre}</h2>
                  <p className="deposito-card-address">
                    <MapPin size={15} />
                    {deposito.direccion}
                  </p>
                  <p className="deposito-card-commerce">
                    <Building2 size={15} />
                    {deposito.comercioNombre || "Sin comercio asociado"}
                  </p>
                </div>

                <button
                  type="button"
                  className="deposito-detail-button"
                  onClick={() => navigate(`/depositos/${deposito.id}`)}
                >
                  <Eye size={16} />
                  Ver detalle
                </button>
              </article>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}

function SummaryCard({ icon: Icon, label, value, tone }) {
  return (
    <div className={`depositos-summary-card ${tone}`}>
      <span className="depositos-summary-icon">
        <Icon size={18} />
      </span>
      <span className="depositos-summary-value">{value}</span>
      <span className="depositos-summary-label">{label}</span>
    </div>
  );
}
