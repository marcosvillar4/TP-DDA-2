import { useState } from "react";
import { InventarioButton } from "./InventarioButton";
import "../styles/ItemInventarioTable.css";

export function ItemInventarioTable({ items, productos, depositos, onEditar, onEliminar }) {
  const productosPorId = new Map(productos.map((p) => [p.id, p.nombre]));
  const depositosPorId = new Map(depositos.map((d) => [d.id, d.nombre]));
  const [editandoId, setEditandoId] = useState(null);

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
          {items.map((item) =>
            editandoId === item.id ? (
              <ItemInventarioEditRow
                key={item.id}
                item={item}
                productos={productos}
                depositos={depositos}
                onCancelar={() => setEditandoId(null)}
                onGuardar={async (datos) => {
                  const exito = await onEditar(item.id, datos);
                  if (exito) setEditandoId(null);
                }}
              />
            ) : (
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
                onEditar={() => setEditandoId(item.id)}
                onEliminar={() => onEliminar(item.id)}
              />
            )
          )}
        </tbody>
      </table>
    </div>
  );
}

function ItemInventarioRow({ nombreProducto, nombreDeposito, item, onEditar, onEliminar }) {
  return (
    <tr>
      <td>{nombreProducto}</td>
      <td>{nombreDeposito}</td>
      <td>{item.cantidad}</td>
      <td className="item-inventario-table-actions">
        <InventarioButton variant="ghost" onClick={onEditar}>
          Editar
        </InventarioButton>
        <InventarioButton variant="danger" onClick={onEliminar}>
          Eliminar
        </InventarioButton>
      </td>
    </tr>
  );
}

function ItemInventarioEditRow({ item, productos, depositos, onGuardar, onCancelar }) {
  const [productoId, setProductoId] = useState(item.productoId);
  const [depositoId, setDepositoId] = useState(item.depositoId);
  const [cantidad, setCantidad] = useState(item.cantidad);
  const [guardando, setGuardando] = useState(false);

  async function handleGuardar() {
    setGuardando(true);
    await onGuardar({ productoId, depositoId, cantidad });
    setGuardando(false);
  }

  return (
    <tr className="item-inventario-table-row-editing">
      <td>
        <select
          className="item-inventario-table-select"
          value={productoId}
          onChange={(e) => setProductoId(e.target.value)}
        >
          {productos.map((producto) => (
            <option key={producto.id} value={producto.id}>
              {producto.nombre}
            </option>
          ))}
        </select>
      </td>
      <td>
        <select
          className="item-inventario-table-select"
          value={depositoId}
          onChange={(e) => setDepositoId(e.target.value)}
        >
          {depositos.map((deposito) => (
            <option key={deposito.id} value={deposito.id}>
              {deposito.nombre}
            </option>
          ))}
        </select>
      </td>
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
        <InventarioButton onClick={handleGuardar} disabled={guardando}>
          {guardando ? "Guardando..." : "Guardar"}
        </InventarioButton>
        <InventarioButton variant="ghost" onClick={onCancelar} disabled={guardando}>
          Cancelar
        </InventarioButton>
      </td>
    </tr>
  );
}