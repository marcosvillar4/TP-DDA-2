import { InventarioButton } from "../../components/InventarioButton";
import { ProductoEstadoBadge } from "./ProductoEstadoBadge";
import "../styles/ProductoModal.css";

export function ProductoDetailModal({ producto, onClose, onVerInventario }) {
  return (
    <div className="producto-modal-backdrop" role="presentation">
      <div className="producto-modal producto-detail-modal" role="dialog" aria-modal="true">
        <div className="producto-modal-header">
          <div>
            <h2>Detalle del producto</h2>
            <p>{producto.nombre}</p>
          </div>
          <button
            type="button"
            className="producto-modal-close"
            onClick={onClose}
            aria-label="Cerrar"
          >
            ×
          </button>
        </div>

        <div className="producto-detail-grid">
          <div>
            <span>Nombre</span>
            <strong>{producto.nombre}</strong>
          </div>
          <div>
            <span>SKU</span>
            <strong>{producto.sku}</strong>
          </div>
          <div>
            <span>Estado</span>
            <ProductoEstadoBadge estado={producto.estado} />
          </div>
          <div>
            <span>Comercio propietario</span>
            <strong>{producto.comercioNombre ?? `Comercio Nº ${producto.comercioId}`}</strong>
          </div>
          <div>
            <span>Categoría</span>
            <strong>{producto.categoria}</strong>
          </div>
          <div className="producto-detail-full">
            <span>Descripción</span>
            <p>{producto.descripcion || "Sin descripción registrada."}</p>
          </div>
        </div>

        <section className="producto-inventario-info">
          <h3>Información de inventario</h3>
          <p>
            El stock y la distribución por depósitos se administran desde
            Inventario.
          </p>
          <InventarioButton variant="ghost" onClick={onVerInventario}>
            Ver en Inventario
          </InventarioButton>
        </section>
      </div>
    </div>
  );
}
