import { MoreVertical } from "lucide-react";
import { useState } from "react";
import { ProductoEstadoBadge } from "./ProductoEstadoBadge";
import "../styles/ProductoTable.css";

export function ProductoTable({
  productos,
  loading,
  onVerDetalle,
  onEditar,
  onCambiarEstado,
}) {
  const [openMenuId, setOpenMenuId] = useState(null);

  if (loading) {
    return (
      <section className="productos-table-card productos-table-state">
        Cargando productos...
      </section>
    );
  }

  return (
    <section className="productos-table-card">
      <div className="productos-table-responsive">
        <table className="productos-table">
          <thead>
            <tr>
              <th>Producto</th>
              <th>SKU</th>
              <th>Comercio</th>
              <th>Categoría</th>
              <th>Estado</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {productos.map((producto) => (
              <tr key={producto.id}>
                <td>
                  <span className="producto-table-name">{producto.nombre}</span>
                  {producto.descripcion && (
                    <span className="producto-table-description">
                      {producto.descripcion}
                    </span>
                  )}
                </td>
                <td className="producto-table-sku">{producto.sku}</td>
                <td>{producto.comercioNombre ?? `Comercio Nº ${producto.comercioId}`}</td>
                <td>{producto.categoria}</td>
                <td>
                  <ProductoEstadoBadge estado={producto.estado} />
                </td>
                <td className="producto-actions-cell">
                  <button
                    type="button"
                    className="producto-menu-trigger"
                    aria-label={`Acciones de ${producto.nombre}`}
                    onClick={() =>
                      setOpenMenuId((current) =>
                        current === producto.id ? null : producto.id
                      )
                    }
                  >
                    <MoreVertical size={18} />
                  </button>

                  {openMenuId === producto.id && (
                    <div className="producto-actions-menu">
                      <button
                        type="button"
                        onClick={() => {
                          setOpenMenuId(null);
                          onVerDetalle(producto);
                        }}
                      >
                        Ver detalle
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setOpenMenuId(null);
                          onEditar(producto);
                        }}
                      >
                        Editar
                      </button>
                      <button
                        type="button"
                        className={
                          producto.estado === "ACTIVO"
                            ? "producto-menu-danger"
                            : ""
                        }
                        onClick={() => {
                          setOpenMenuId(null);
                          onCambiarEstado(producto);
                        }}
                      >
                        {producto.estado === "ACTIVO" ? "Desactivar" : "Activar"}
                      </button>
                    </div>
                  )}
                </td>
              </tr>
            ))}

            {productos.length === 0 && (
              <tr>
                <td colSpan="6" className="productos-table-empty">
                  No se encontraron productos con los filtros aplicados.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
