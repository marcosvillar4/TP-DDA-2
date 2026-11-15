import { useState } from "react";
import { Building2, CheckCircle2, Edit2, PauseCircle, Plus, Search, Truck } from "lucide-react";
import { useTransportistas } from "../../hooks/useTransportistas";
import TransportistaEstadoBadge from "./TransportistaEstadoBadge";
import TransportistaFormModal from "./TransportistaFormModal";
import "./styles/TransportistasPage.css";

export default function TransportistasPage() {
  const { transportistas, loading, saving, error, buscar, estado, kpis, setBuscar, setEstado, guardar, cambiarEstado, cambiarActivo } = useTransportistas();
  const [modal, setModal] = useState({ open: false, item: null });

  async function submit(payload) {
    if (await guardar(payload, modal.item?.id)) setModal({ open: false, item: null });
  }

  return (
    <div className="transportistas-page">
      <div className="transportistas-header">
        <div><h1>Transportistas</h1><p>{transportistas.length} transportista{transportistas.length !== 1 ? "s" : ""} mostrado{transportistas.length !== 1 ? "s" : ""}</p></div>
        <button className="transportistas-primary" onClick={() => setModal({ open: true, item: null })}><Plus size={16} /> Nuevo transportista</button>
      </div>
      <div className="transportistas-kpis">
        <Kpi icon={Building2} label="Total" value={kpis.total} tone="blue" />
        <Kpi icon={CheckCircle2} label="Disponibles" value={kpis.disponibles} tone="green" />
        <Kpi icon={Truck} label="En servicio" value={kpis.enServicio} tone="violet" />
        <Kpi icon={PauseCircle} label="No disponibles" value={kpis.noDisponibles} tone="amber" />
      </div>
      <div className="transportistas-filters">
        <div className="transportistas-search"><Search size={18} /><input placeholder="Buscar por empresa..." value={buscar} onChange={(event) => setBuscar(event.target.value)} /></div>
        <select value={estado} onChange={(event) => setEstado(event.target.value)}>
          <option value="">Todos los estados</option><option value="DISPONIBLE">Disponibles</option><option value="EN_SERVICIO">En servicio</option><option value="NO_DISPONIBLE">No disponibles</option>
        </select>
      </div>
      <div className="transportistas-table-card">
        {loading ? <div className="transportistas-state">Cargando transportistas...</div> : error ? <div className="transportistas-state error">{error}</div> : (
          <div className="transportistas-table-responsive"><table className="transportistas-table"><thead><tr><th>Empresa</th><th>Estado</th><th>Activo</th><th>Acciones</th></tr></thead><tbody>
            {transportistas.map((item) => <tr key={item.id}><td><strong>{item.empresa}</strong><small>ID #{item.id}</small></td><td><TransportistaEstadoBadge estado={item.estado} activo={item.activo} /></td><td>{item.activo ? "Sí" : "No"}</td><td className="transportistas-actions">
              <button onClick={() => setModal({ open: true, item })} disabled={saving} title="Editar"><Edit2 size={15} /> Editar</button>
              <select value={item.estado || ""} onChange={(event) => cambiarEstado(item.id, event.target.value)} disabled={saving || !item.activo} aria-label={`Estado de ${item.empresa}`}>
                <option value="DISPONIBLE">Disponible</option><option value="EN_SERVICIO">En servicio</option><option value="NO_DISPONIBLE">No disponible</option>
              </select>
              <button onClick={() => cambiarActivo(item)} disabled={saving}>{item.activo ? "Desactivar" : "Activar"}</button>
            </td></tr>)}
            {!transportistas.length && <tr><td colSpan="4" className="transportistas-state">No se encontraron transportistas.</td></tr>}
          </tbody></table></div>
        )}
      </div>
      <TransportistaFormModal key={`${modal.open}-${modal.item?.id || "nuevo"}`} open={modal.open} transportista={modal.item} saving={saving} onClose={() => setModal({ open: false, item: null })} onSubmit={submit} />
    </div>
  );
}

function Kpi({ icon: Icon, label, value, tone }) {
  return <div className={`transportistas-kpi ${tone}`}><span><Icon size={18} /></span><strong>{value}</strong><small>{label}</small></div>;
}
