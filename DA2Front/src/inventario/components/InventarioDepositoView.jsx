import { useInventarioDeposito } from "../../../hooks/useInventarioDeposito";
import { InventarioButton } from "./InventarioButton";
import "../styles/ItemInventarioTable.css";
import "../styles/InventarioDepositoView.css";

const STOCK_MINIMO_DEFAULT = 5;

function stockMinimoDe(item) {
  return item.stockMinimo ?? STOCK_MINIMO_DEFAULT;
}

/** Vista del rol DEPOSITO: stock de su depósito, en solo lectura. */
export function InventarioDepositoView() {
  const { deposito, items, loading, sinDeposito, error, recargar } =
    useInventarioDeposito();

  if (loading) {
    return <p className="inventario-loading-text">Cargando inventario...</p>;
  }

  if (sinDeposito) {
    return (
      <div className="inventario-content">
        <section className="inventario-panel">
          <p>
            Tu usuario todavía no tiene un depósito asociado. Pedile a un
            administrador que lo revise.
          </p>
        </section>
      </div>
    );
  }

  if (error) {
    return (
      <div className="inventario-content">
        <section className="inventario-panel inventario-panel-empty">
          <p>{error}</p>
          <InventarioButton onClick={recargar}>Reintentar</InventarioButton>
        </section>
      </div>
    );
  }

  const unidades = items.reduce((total, it) => total + (it.cantidad ?? 0), 0);
  const bajoStock = items.filter((it) => (it.cantidad ?? 0) <= stockMinimoDe(it)).length;
  const itemsOrdenados = [...items].sort((a, b) =>
    (a.productoNombre ?? "").localeCompare(b.productoNombre ?? "")
  );

  return (
    <div className="inventario-content">
      <section className="inventario-panel">
        <h2 className="inventario-panel-title">{deposito.nombre || deposito.nombreDeposito}</h2>
        <p className="inventario-deposito-info">
          {deposito.direccion}
          {deposito.comercioNombre ? ` · ${deposito.comercioNombre}` : ""}
        </p>

        <div className="inventario-deposito-stats">
          <div className="inventario-deposito-stat">
            <strong>{items.length}</strong>
            <span>{items.length === 1 ? "ítem" : "ítems"}</span>
          </div>
          <div className="inventario-deposito-stat">
            <strong>{unidades}</strong>
            <span>unidades en total</span>
          </div>
          <div className="inventario-deposito-stat">
            <strong>{bajoStock}</strong>
            <span>con stock bajo</span>
          </div>
        </div>
      </section>

      <section className="inventario-panel">
        <h2 className="inventario-panel-title">Inventario del depósito</h2>

        {itemsOrdenados.length === 0 ? (
          <p className="item-inventario-table-empty">
            Todavía no hay productos almacenados en este depósito.
          </p>
        ) : (
          <div className="item-inventario-table-wrapper">
            <table className="item-inventario-table">
              <thead>
                <tr>
                  <th>SKU</th>
                  <th>Producto</th>
                  <th>Comercio</th>
                  <th>Cantidad</th>
                  <th>Stock mínimo</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                {itemsOrdenados.map((item) => {
                  const bajo = (item.cantidad ?? 0) <= stockMinimoDe(item);
                  return (
                    <tr key={item.id}>
                      <td>{item.productoSku ?? "—"}</td>
                      <td>{item.productoNombre ?? `Producto Nº ${item.productoId}`}</td>
                      <td>{item.comercioNombre ?? "—"}</td>
                      <td>{item.cantidad}</td>
                      <td>{stockMinimoDe(item)}</td>
                      <td>
                        <span
                          className={`inventario-deposito-badge ${
                            bajo ? "inventario-deposito-badge-bajo" : "inventario-deposito-badge-ok"
                          }`}
                        >
                          {bajo ? "Stock bajo" : "OK"}
                        </span>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}
