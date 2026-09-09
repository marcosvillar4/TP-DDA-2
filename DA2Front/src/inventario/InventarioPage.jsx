import { useState } from "react";
import { useInventario } from "../../hooks/useInventario";
import { InventarioTopBar } from "./components/InventarioTopBar";
import { Select } from "./components/Select";
import { InventarioButton } from "./components/InventarioButton";
import { InventarioSummary } from "./components/InventarioSummary";
import { ItemInventarioForm } from "./components/ItemInventarioForm";
import { ItemInventarioTable } from "./components/ItemInventarioTable";
import ProductosPage from "./productos/ProductosPage";
import "./styles/InventarioPage.css";

export default function InventarioPage() {
  const [activeTab, setActiveTab] = useState("stock");
  const {
    comercios,
    productos,
    depositos,
    comercioId,
    inventario,
    items,
    loadingComercios,
    loadingInventario,
    creatingInventario,
    submittingItem,
    handleSelectComercio,
    handleCrearInventario,
    handleEliminarInventario,
    handleAgregarItem,
    handleActualizarCantidad,
    handleEliminarItem,
  } = useInventario();

  return (
    <div className="inventario-page">
      <InventarioTopBar />

      <main
        className={`inventario-content ${
          activeTab === "productos" ? "inventario-content-wide" : ""
        }`.trim()}
      >
        <div className="inventario-tabs" role="tablist" aria-label="Inventario">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === "stock"}
            className={`inventario-tab ${
              activeTab === "stock" ? "inventario-tab-active" : ""
            }`.trim()}
            onClick={() => setActiveTab("stock")}
          >
            Stock
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === "productos"}
            className={`inventario-tab ${
              activeTab === "productos" ? "inventario-tab-active" : ""
            }`.trim()}
            onClick={() => setActiveTab("productos")}
          >
            Productos
          </button>
        </div>

        {activeTab === "stock" && (
          <>
            <section className="inventario-panel">
              <h2 className="inventario-panel-title">Comercio</h2>
              <Select
                id="comercio"
                label="Seleccioná un comercio"
                value={comercioId}
                onChange={(e) => handleSelectComercio(e.target.value)}
                disabled={loadingComercios}
              >
                <option value="">
                  {loadingComercios
                    ? "Cargando comercios..."
                    : "Elegí un comercio"}
                </option>
                {comercios.map((comercio) => (
                  <option key={comercio.id} value={comercio.id}>
                    {comercio.nombre}
                  </option>
                ))}
              </Select>
            </section>

            {comercioId && loadingInventario && (
              <p className="inventario-loading-text">Cargando inventario...</p>
            )}

            {comercioId && !loadingInventario && !inventario && (
              <section className="inventario-panel inventario-panel-empty">
                <p>Este comercio todavía no tiene un inventario creado.</p>
                <InventarioButton
                  onClick={handleCrearInventario}
                  disabled={creatingInventario}
                >
                  {creatingInventario ? "Creando..." : "Crear inventario"}
                </InventarioButton>
              </section>
            )}

            {inventario && (
              <>
                <InventarioSummary
                  inventario={inventario}
                  totalItems={items.length}
                  onEliminar={handleEliminarInventario}
                />

                <section className="inventario-panel">
                  <h2 className="inventario-panel-title">Agregar ítem</h2>
                  <ItemInventarioForm
                    productos={productos}
                    depositos={depositos}
                    loading={submittingItem}
                    onSubmit={handleAgregarItem}
                  />
                </section>

                <section className="inventario-panel">
                  <h2 className="inventario-panel-title">
                    Ítems del inventario
                  </h2>
                  <ItemInventarioTable
                    items={items}
                    productos={productos}
                    depositos={depositos}
                    onActualizarCantidad={handleActualizarCantidad}
                    onEliminar={handleEliminarItem}
                  />
                </section>
              </>
            )}
          </>
        )}

        {activeTab === "productos" && (
          <ProductosPage onVerInventario={() => setActiveTab("stock")} />
        )}
      </main>
    </div>
  );
}
