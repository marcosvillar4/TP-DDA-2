import { useState } from "react";
import { X } from "lucide-react";
import "../styles/RepartidorFormModal.css";

export default function AsignarPedidoModal({
  open,
  repartidor,
  pedidos = [],
  saving = false,
  onClose,
  onConfirm,
}) {
  const [pedidoId, setPedidoId] = useState("");

  if (!open) return null;

  async function handleSubmit(event) {
    event.preventDefault();
    if (!pedidoId) return;
    const ok = await onConfirm(Number(pedidoId));
    if (ok) {
      setPedidoId("");
    }
  }

  return (
    <div className="repartidor-modal-backdrop" role="presentation">
      <div className="repartidor-modal repartidor-modal-small" role="dialog" aria-modal="true">
        <div className="repartidor-modal-header">
          <div>
            <h2>Asignar pedido</h2>
            <p>Asignar pedido pendiente a {repartidor?.nombreCompleto}</p>
          </div>
          <button className="repartidor-modal-close" onClick={onClose} type="button">
            <X size={18} />
          </button>
        </div>

        <form className="repartidor-form" onSubmit={handleSubmit}>
          <label className="repartidor-field repartidor-field-full">
            <span>Pedido pendiente</span>
            <select
              value={pedidoId}
              onChange={(event) => setPedidoId(event.target.value)}
              disabled={saving || pedidos.length === 0}
              required
            >
              <option value="">Seleccionar pedido...</option>
              {pedidos.map((pedido) => (
                <option key={pedido.id} value={pedido.id}>
                  #{pedido.id} - {pedido.direccionDestino}
                </option>
              ))}
            </select>
            {pedidos.length === 0 && <small>No hay pedidos CREADO sin repartidor asignado.</small>}
          </label>

          <div className="repartidor-form-actions">
            <button type="button" className="repartidor-btn-secondary" onClick={onClose}>
              Cancelar
            </button>
            <button type="submit" className="repartidor-btn-primary" disabled={saving || !pedidoId}>
              Confirmar asignación
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
