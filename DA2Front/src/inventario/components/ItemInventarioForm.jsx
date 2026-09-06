import { useState } from "react";
import { TextField } from "../../auth/components/TextField";
import { Select } from "./Select";
import { InventarioButton } from "./InventarioButton";
import "../styles/ItemInventarioForm.css";

export function ItemInventarioForm({ productos, depositos, loading, onSubmit }) {
  const [productoId, setProductoId] = useState("");
  const [depositoId, setDepositoId] = useState("");
  const [cantidad, setCantidad] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();

    const success = await onSubmit({ productoId, depositoId, cantidad });

    if (success) {
      setProductoId("");
      setDepositoId("");
      setCantidad("");
    }
  }

  return (
    <form onSubmit={handleSubmit} className="item-inventario-form">
      <Select
        id="producto"
        label="Producto"
        value={productoId}
        onChange={(e) => setProductoId(e.target.value)}
      >
        <option value="">Seleccioná un producto</option>
        {productos.map((producto) => (
          <option key={producto.id} value={producto.id}>
            {producto.nombre}
          </option>
        ))}
      </Select>

      <Select
        id="deposito"
        label="Depósito"
        value={depositoId}
        onChange={(e) => setDepositoId(e.target.value)}
        disabled={depositos.length === 0}
      >
        <option value="">
          {depositos.length === 0
            ? "Sin depósitos disponibles"
            : "Seleccioná un depósito"}
        </option>
        {depositos.map((deposito) => (
          <option key={deposito.id} value={deposito.id}>
            {deposito.nombre}
          </option>
        ))}
      </Select>

      <TextField
        id="cantidad"
        label="Cantidad"
        type="number"
        min="0"
        placeholder="Ej: 10"
        value={cantidad}
        onChange={(e) => setCantidad(e.target.value)}
      />

      <InventarioButton
        type="submit"
        disabled={loading}
        className="item-inventario-form-submit"
      >
        {loading ? "Guardando..." : "Agregar ítem"}
      </InventarioButton>
    </form>
  );
}