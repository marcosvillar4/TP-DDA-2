import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Edit2, Mail, MapPin, Phone, Route, Truck, UserPlus } from "lucide-react";
import { useRepartidorDetalle } from "../../hooks/useRepartidores";
import RepartidorEstadoBadge from "./components/RepartidorEstadoBadge";
import RepartidorFormModal from "./components/RepartidorFormModal";
import AsignarPedidoModal from "./components/AsignarPedidoModal";
import "./styles/RepartidorDetail.css";

export default function RepartidorDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const {
    repartidor,
    historial,
    pedidosAsignables,
    loading,
    saving,
    error,
    guardarCambios,
    asignarPedidoPendiente,
    cambiarEstadoDetalle,
    cambiarActivoDetalle,
  } = useRepartidorDetalle(id);

  const [isEditOpen, setIsEditOpen] = useState(false);
  const [isAssignOpen, setIsAssignOpen] = useState(false);

  if (loading) {
    return <div className="repartidor-detail-state">Cargando repartidor...</div>;
  }

  if (error || !repartidor) {
    return <div className="repartidor-detail-state error">{error || "Repartidor no encontrado"}</div>;
  }

  const puedeAsignar = repartidor.activo && repartidor.estado === "DISPONIBLE";
  const tienePedidoActual = Boolean(repartidor.pedidoActual);
  const puedeCambiarDisponibilidad =
    repartidor.activo && !tienePedidoActual && repartidor.estado !== "EN_ENTREGA";
  const desactivarDeshabilitado = saving || (repartidor.activo && tienePedidoActual);

  async function handleSave(payload) {
    const ok = await guardarCambios(payload);
    if (ok) {
      setIsEditOpen(false);
    }
  }

  async function handleAssign(pedidoId) {
    const ok = await asignarPedidoPendiente(pedidoId);
    if (ok) {
      setIsAssignOpen(false);
    }
    return ok;
  }

  return (
    <div className="repartidor-detail-page">
      <button className="repartidor-back-btn" onClick={() => navigate("/repartidores")}>
        <ArrowLeft size={16} />
        Volver a Repartidores
      </button>

      <div className="repartidor-detail-header">
        <div>
          <div className="repartidor-detail-title-row">
            <h1>{repartidor.nombreCompleto}</h1>
            <RepartidorEstadoBadge estado={repartidor.estado} activo={repartidor.activo} />
          </div>
          <p>{repartidor.zona}</p>
        </div>
        <div className="repartidor-detail-actions">
          {puedeAsignar && (
            <button className="repartidor-btn-primary" onClick={() => setIsAssignOpen(true)}>
              <UserPlus size={16} />
              Asignar pedido
            </button>
          )}
          <button className="repartidor-btn-outline" onClick={() => setIsEditOpen(true)}>
            <Edit2 size={16} />
            Editar
          </button>
        </div>
      </div>

      <div className="repartidor-detail-grid">
        <main className="repartidor-detail-main">
          <section className="repartidor-card">
            <div className="repartidor-card-header">
              <h2>Historial de entregas</h2>
              <span>{historial.length} registro{historial.length !== 1 ? "s" : ""}</span>
            </div>
            <div className="repartidor-history-table-wrapper">
              <table className="repartidor-history-table">
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Pedido</th>
                    <th>Dirección de entrega</th>
                    <th>Resultado</th>
                  </tr>
                </thead>
                <tbody>
                  {historial.map((item) => (
                    <tr key={item.pedidoId}>
                      <td>{item.fecha || "—"}</td>
                      <td className="repartidor-pedido-id">#{item.pedidoId}</td>
                      <td>{item.direccionEntrega}</td>
                      <td>{formatPedidoEstado(item.resultado)}</td>
                    </tr>
                  ))}
                  {historial.length === 0 && (
                    <tr>
                      <td className="repartidor-empty" colSpan="4">
                        Todavía no hay entregas finalizadas para este repartidor.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </main>

        <aside className="repartidor-detail-side">
          <section className="repartidor-card">
            <h2>Datos de contacto</h2>
            <Info icon={Phone} label="Teléfono" value={repartidor.telefono} />
            <Info icon={Mail} label="Email" value={repartidor.email} />
            <Info icon={MapPin} label="Zona asignada" value={repartidor.zona} />
          </section>

          <section className="repartidor-card">
            <h2>Vehículo</h2>
            <Info icon={Truck} label="Tipo" value={repartidor.tipoVehiculo} />
            <Info icon={Route} label="Patente" value={repartidor.patente} />
          </section>

          <section className="repartidor-card">
            <h2>Pedido en curso</h2>
            {repartidor.pedidoActual ? (
              <div className="repartidor-current-order">
                <strong>#{repartidor.pedidoActual.id}</strong>
                <span>{formatPedidoEstado(repartidor.pedidoActual.estado)}</span>
                <small>{repartidor.pedidoActual.direccionDestino}</small>
              </div>
            ) : (
              <p className="repartidor-muted">Sin pedido asignado</p>
            )}
          </section>

          <section className="repartidor-card">
            <h2>Operación</h2>
            <div className="repartidor-stack-actions">
              {puedeCambiarDisponibilidad && (
                <>
                  <button
                    className="repartidor-btn-outline full"
                    onClick={() => cambiarEstadoDetalle("DISPONIBLE")}
                    disabled={saving || repartidor.estado === "DISPONIBLE"}
                  >
                    Marcar disponible
                  </button>
                  <button
                    className="repartidor-btn-outline full"
                    onClick={() => cambiarEstadoDetalle("NO_DISPONIBLE")}
                    disabled={saving || repartidor.estado === "NO_DISPONIBLE"}
                  >
                    Marcar no disponible
                  </button>
                </>
              )}
              <button
                className="repartidor-btn-danger full"
                onClick={() => cambiarActivoDetalle(!repartidor.activo)}
                disabled={desactivarDeshabilitado}
                title={
                  repartidor.activo && tienePedidoActual
                    ? "No se puede desactivar un repartidor con un pedido activo"
                    : undefined
                }
              >
                {repartidor.activo ? "Desactivar" : "Activar"}
              </button>
            </div>
          </section>
        </aside>
      </div>

      <RepartidorFormModal
        open={isEditOpen}
        mode="edit"
        repartidor={repartidor}
        saving={saving}
        onClose={() => setIsEditOpen(false)}
        onSubmit={handleSave}
      />

      <AsignarPedidoModal
        open={isAssignOpen}
        repartidor={repartidor}
        pedidos={pedidosAsignables}
        saving={saving}
        onClose={() => setIsAssignOpen(false)}
        onConfirm={handleAssign}
      />
    </div>
  );
}

function Info({ icon: Icon, label, value }) {
  return (
    <div className="repartidor-info-item">
      <Icon size={16} />
      <span>
        <small>{label}</small>
        <strong>{value || "—"}</strong>
      </span>
    </div>
  );
}

function formatPedidoEstado(estado) {
  const labels = {
    CREADO: "Creado",
    ASIGNADO: "Asignado",
    EN_CAMINO: "En camino",
    ENTREGADO: "Entregado",
    CANCELADO: "No entregado",
  };
  return labels[estado] || estado || "—";
}
