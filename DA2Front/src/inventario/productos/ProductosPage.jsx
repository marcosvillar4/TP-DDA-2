import { useState } from "react";
import { Package, PackageCheck, PackageX, Plus } from "lucide-react";
import KpiCard from "../../dashboard/components/KpiCard";
import { useProductos } from "../../../hooks/useProductos";
import { InventarioButton } from "../components/InventarioButton";
import { ProductoDetailModal } from "./components/ProductoDetailModal";
import { ProductoFilters } from "./components/ProductoFilters";
import { ProductoFormModal } from "./components/ProductoFormModal";
import { ProductoTable } from "./components/ProductoTable";
import "./styles/ProductosPage.css";

export default function ProductosPage({ onVerInventario }) {
  const {
    productos,
    comercios,
    categorias,
    filtros,
    kpis,
    loading,
    submitting,
    detalleProducto,
    setDetalleProducto,
    actualizarFiltro,
    limpiarFiltros,
    registrarProducto,
    editarProducto,
    verDetalle,
    cambiarEstado,
  } = useProductos();

  const [formMode, setFormMode] = useState(null);
  const [productoEditando, setProductoEditando] = useState(null);

  function abrirCrear() {
    setProductoEditando(null);
    setFormMode("create");
  }

  function abrirEditar(producto) {
    setProductoEditando(producto);
    setFormMode("edit");
  }

  function cerrarFormulario() {
    setProductoEditando(null);
    setFormMode(null);
  }

  async function handleSubmit(form) {
    const success =
      formMode === "create"
        ? await registrarProducto(form)
        : await editarProducto(productoEditando, form);

    if (success) {
      cerrarFormulario();
    }
  }

  return (
    <section className="productos-page">
      <div className="productos-header">
        <div>
          <h1 className="productos-title">Productos</h1>
          <p className="productos-subtitle">
            Administrá los productos registrados por los comercios
          </p>
        </div>
        <InventarioButton onClick={abrirCrear} className="productos-new-button">
          <Plus size={16} />
          Nuevo producto
        </InventarioButton>
      </div>

      <div className="productos-kpis">
        <KpiCard
          label="Productos registrados"
          value={kpis.total}
          sub="Total del catálogo"
          icon={Package}
          color="blue"
        />
        <KpiCard
          label="Productos activos"
          value={kpis.activos}
          sub="Disponibles para operación"
          icon={PackageCheck}
          color="green"
        />
        <KpiCard
          label="Productos inactivos"
          value={kpis.inactivos}
          sub="Con baja lógica"
          icon={PackageX}
          color="red"
        />
      </div>

      <ProductoFilters
        filtros={filtros}
        comercios={comercios}
        categorias={categorias}
        onChange={actualizarFiltro}
        onClear={limpiarFiltros}
      />

      <ProductoTable
        productos={productos}
        loading={loading}
        onVerDetalle={verDetalle}
        onEditar={abrirEditar}
        onCambiarEstado={cambiarEstado}
      />

      {formMode && (
        <ProductoFormModal
          key={formMode === "edit" ? productoEditando.id : "create"}
          mode={formMode}
          producto={productoEditando}
          comercios={comercios}
          categorias={categorias}
          submitting={submitting}
          onClose={cerrarFormulario}
          onSubmit={handleSubmit}
        />
      )}

      {detalleProducto && (
        <ProductoDetailModal
          producto={detalleProducto}
          onClose={() => setDetalleProducto(null)}
          onVerInventario={() => {
            setDetalleProducto(null);
            onVerInventario?.();
          }}
        />
      )}
    </section>
  );
}
