import { useState } from "react";
import { Plus, Warehouse } from "lucide-react";
import { useDepositos } from "../../hooks/useDepositos";
import { DepositoFormModal } from "./components/DepositoFormModal";
import { labelEstadoComercio } from "../comercios/utils/comercioEstado";
import "./styles/DepositosPage.css";

/** Depósitos vinculados al comercio del usuario logueado (rol COMERCIO). */
export default function DepositosPage() {
  const { comercio, depositos, loading, submitting, error, handleAgregarDeposito } =
    useDepositos();
  const [modalAbierto, setModalAbierto] = useState(false);

  return (
    <div className="depositos-page">
      <div className="depositos-header">
        <div>
          <h1 className="depositos-title">Mis depósitos</h1>
          <p className="depositos-subtitle">
            {comercio
              ? `${depositos.length} depósito${depositos.length !== 1 ? "s" : ""} vinculado${depositos.length !== 1 ? "s" : ""} a ${comercio.nombreComercial}`
              : "Depósitos vinculados a tu comercio"}
          </p>
        </div>
        <button
          type="button"
          className="depositos-btn-nuevo"
          onClick={() => setModalAbierto(true)}
          disabled={!comercio}
        >
          <Plus size={16} /> Agregar depósito
        </button>
      </div>

      {loading && <p className="depositos-message">Cargando depósitos...</p>}
      {!loading && error && <p className="depositos-message">{error}</p>}

      {!loading && !error && depositos.length === 0 && (
        <div className="depositos-empty">
          <Warehouse size={32} />
          <p>Todavía no agregaste ningún depósito.</p>
        </div>
      )}

      {!loading && !error && depositos.length > 0 && (
        <div className="depositos-grid">
          {depositos.map((d) => (
            <div key={d.id} className="deposito-card">
              <div className="deposito-card-header">
                <Warehouse size={18} />
                <h3>{d.nombre}</h3>
              </div>
              <dl className="deposito-card-data">
                <div>
                  <dt>Dirección</dt>
                  <dd>{d.direccion}</dd>
                </div>
                <div>
                  <dt>Responsable</dt>
                  <dd>{d.responsable ?? "—"}</dd>
                </div>
                <div>
                  <dt>Email</dt>
                  <dd>{d.usuarioEmail ?? "—"}</dd>
                </div>
                <div>
                  <dt>Estado de la cuenta</dt>
                  <dd>{d.usuarioEstado ? labelEstadoComercio(d.usuarioEstado) : "—"}</dd>
                </div>
              </dl>
            </div>
          ))}
        </div>
      )}

      {modalAbierto && (
        <DepositoFormModal
          submitting={submitting}
          onClose={() => setModalAbierto(false)}
          onSubmit={handleAgregarDeposito}
        />
      )}
    </div>
  );
}
