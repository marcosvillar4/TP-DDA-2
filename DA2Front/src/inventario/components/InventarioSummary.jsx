import { InventarioButton } from "./InventarioButton";
import "../styles/InventarioSummary.css";

export function InventarioSummary({ inventario, totalItems, onEliminar }) {
  return (
    <section className="inventario-panel inventario-summary">
      <div>
        <h2 className="inventario-panel-title">Inventario Nº {inventario.id}</h2>
        <p className="inventario-summary-text">
          Comercio Nº {inventario.comercioId} · {totalItems}{" "}
          {totalItems === 1 ? "ítem cargado" : "ítems cargados"}
        </p>
      </div>
      <InventarioButton variant="danger" onClick={onEliminar}>
        Eliminar inventario
      </InventarioButton>
    </section>
  );
}