import { useState } from "react";
import { InventarioButton } from "./InventarioButton";
import "../styles/ItemInventarioTable.css";

export function ItemInventarioTable({
  items,
  productos,
  depositos,
  onActualizarCantidad,
  onEliminar,
}) {
  const productosPorId = new Map(productos.map((p) => [p.id, p.nombre]));
  const depositosPorId = new Map(depositos.map((d) => [d.id, d.nombre]));

  if (items.length === 0) {
    return (
      <p className="item-inventario-table-empty">
        Todavía no hay ítems cargados en este inventario.
      </p>
    );
  }

  return (
    <div className="item-inventario-table-wrapper">
      <table className="item-inventario-table">
        <thead>
          <tr>
            <th>Producto</th>
            <th>Depósito</th>
            <th>Cantidad</th>
            <th aria-label="Acciones" />
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <ItemInventarioRow
              key={item.id}
              item={item}
              nombreProducto={
                productosPorId.get(item.productoId) ??
                `Producto Nº ${item.productoId}`
              }
              nombreDeposito={
                depositosPorId.get(item.depositoId) ??
                `Depósito Nº ${item.depositoId}`
              }
              onActualizarCantidad={onActualizarCantidad}
              onEliminar={onEliminar}
            />
          ))}
        </tbody>
      </table>
    </div>
  );
}

function ItemInventarioRow({
  item,
  nombreProducto,
  nombreDeposito,
  onActualizarCantidad,
  onEliminar,
}) {
  const [cantidad, setCantidad] = useState(item.cantidad);
  const cambiada = Number(cantidad) !== item.cantidad;

  return (
    <tr>
      <td>{nombreProducto}</td>
      <td>{nombreDeposito}</td>
      <td>
        <input
          type="number"
          min="0"
          value={cantidad}
          onChange={(e) => setCantidad(e.target.value)}
          className="item-inventario-table-cantidad-input"
        />
      </td>
      <td className="item-inventario-table-actions">
        {cambiada && (
          <InventarioButton
            variant="ghost"
            onClick={() => onActualizarCantidad(item.id, cantidad)}
          >
            Guardar
          </InventarioButton>
        )}
        <InventarioButton variant="danger" onClick={() => onEliminar(item.id)}>
          Eliminar
        </InventarioButton>
      </td>
    </tr>
  );
}