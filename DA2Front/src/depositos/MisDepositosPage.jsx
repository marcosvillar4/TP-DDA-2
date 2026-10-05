import { useState } from "react";
import { Plus, Warehouse } from "lucide-react";
import { MAX_DEPOSITOS_POR_COMERCIO, useMisDepositos } from "../../hooks/useMisDepositos";
import { MisDepositoFormModal } from "./components/MisDepositoFormModal";
import { labelEstadoComercio } from "../comercios/utils/comercioEstado";
import "./styles/MisDepositos.css";

/** Depósitos vinculados al comercio del usuario logueado (rol COMERCIO). */
export default function MisDepositosPage() {
  const {
    comercio,
    depositos,
    loading,
    submitting,
    error,
    limiteAlcanzado,
    handleAgregarDeposito,
  } = useMisDepositos();
  const [modalAbierto, setModalAbierto] = useState(false);

  return (
    <div className="misdep-page">
      <div className="misdep-header">
        <div>
          <h1 className="misdep-title">
            Mis depósitos
            {comercio && (
              <span className="misdep-contador">
                {depositos.length}/{MAX_DEPOSITOS_POR_COMERCIO}
              </span>
            )}
          </h1>
          <p className="misdep-subtitle">
            {comercio
              ? `${depositos.length} depósito${depositos.length !== 1 ? "s" : ""} vinculado${depositos.length !== 1 ? "s" : ""} a ${comercio.nombreComercial}`
              : "Depósitos vinculados a tu comercio"}
          </p>
        </div>
        <button
          type="button"
          className="misdep-btn-nuevo"
          onClick={() => setModalAbierto(true)}
          disabled={!comercio || limiteAlcanzado}
          title={limiteAlcanzado ? "Alcanzaste el límite de depósitos" : undefined}
        >
          <Plus size={16} /> Agregar depósito
        </button>
      </div>

      {limiteAlcanzado && (
        <p className="misdep-limite">
          Alcanzaste el máximo de {MAX_DEPOSITOS_POR_COMERCIO} depósitos por comercio.
        </p>
      )}

      {loading && <p className="misdep-message">Cargando depósitos...</p>}
      {!loading && error && <p className="misdep-message">{error}</p>}

      {!loading && !error && depositos.length === 0 && (
        <div className="misdep-empty">
          <Warehouse size={32} />
          <p>Todavía no agregaste ningún depósito.</p>
        </div>
      )}

      {!loading && !error && depositos.length > 0 && (
        <div className="misdep-grid">
          {depositos.map((d) => (
            <div key={d.id} className="misdep-card">
              <div className="misdep-card-header">
                <Warehouse size={18} />
                <h3>{d.nombre}</h3>
              </div>
              <dl className="misdep-card-data">
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
        <MisDepositoFormModal
          submitting={submitting}
          onClose={() => setModalAbierto(false)}
          onSubmit={handleAgregarDeposito}
        />
      )}
    </div>
  );
}
