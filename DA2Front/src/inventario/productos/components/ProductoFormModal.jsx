import { useMemo, useState } from "react";
import { InventarioButton } from "../../components/InventarioButton";
import "../styles/ProductoModal.css";

const initialForm = {
  nombre: "",
  sku: "",
  descripcion: "",
  categoria: "",
  comercioId: "",
};

export function ProductoFormModal({
  mode,
  producto,
  comercios,
  categorias,
  submitting,
  onClose,
  onSubmit,
}) {
  const isEdit = mode === "edit";
  const [form, setForm] = useState(() => {
    if (isEdit && producto) {
      return {
        nombre: producto.nombre ?? "",
        sku: producto.sku ?? "",
        descripcion: producto.descripcion ?? "",
        categoria: producto.categoria ?? "",
        comercioId: producto.comercioId ?? "",
      };
    }

    return initialForm;
  });

  const comercioNombre = useMemo(() => {
    return (
      producto?.comercioNombre ??
      comercios.find((comercio) => comercio.id === Number(form.comercioId))?.nombre ??
      ""
    );
  }, [comercios, form.comercioId, producto]);

  function handleChange(e) {
    setForm((prev) => ({
      ...prev,
      [e.target.name]: e.target.value,
    }));
  }

  function handleSubmit(e) {
    e.preventDefault();
    onSubmit(form);
  }

  return (
    <div className="producto-modal-backdrop" role="presentation">
      <div className="producto-modal" role="dialog" aria-modal="true">
        <div className="producto-modal-header">
          <div>
            <h2>{isEdit ? "Editar producto" : "Registrar producto"}</h2>
            {!isEdit && (
              <p>El producto se registra automáticamente como activo.</p>
            )}
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

        {isEdit && (
          <div className="producto-readonly-grid">
            <div>
              <span>SKU</span>
              <strong>{producto.sku}</strong>
            </div>
            <div>
              <span>Comercio</span>
              <strong>{comercioNombre}</strong>
            </div>
          </div>
        )}

        <form className="producto-form" onSubmit={handleSubmit}>
          <label>
            <span>Nombre del producto *</span>
            <input
              name="nombre"
              value={form.nombre}
              onChange={handleChange}
              placeholder="Ej: Zapatillas Running X"
            />
          </label>

          {!isEdit && (
            <label>
              <span>SKU *</span>
              <input
                name="sku"
                value={form.sku}
                onChange={handleChange}
                placeholder="Ej: US-001"
              />
            </label>
          )}

          <label>
            <span>Descripción</span>
            <textarea
              name="descripcion"
              value={form.descripcion}
              onChange={handleChange}
              placeholder="Descripción logística del producto"
              rows="3"
            />
          </label>

          <label>
            <span>Categoría *</span>
            <input
              name="categoria"
              value={form.categoria}
              onChange={handleChange}
              list="producto-categorias"
              placeholder="Ej: Calzado"
            />
            <datalist id="producto-categorias">
              {categorias.map((categoria) => (
                <option key={categoria} value={categoria} />
              ))}
            </datalist>
          </label>

          {!isEdit && (
            <label>
              <span>Comercio *</span>
              <select
                name="comercioId"
                value={form.comercioId}
                onChange={handleChange}
              >
                <option value="">Seleccioná un comercio</option>
                {comercios.map((comercio) => (
                  <option key={comercio.id} value={comercio.id}>
                    {comercio.nombre}
                  </option>
                ))}
              </select>
            </label>
          )}

          <div className="producto-modal-actions">
            <InventarioButton variant="ghost" onClick={onClose}>
              Cancelar
            </InventarioButton>
            <InventarioButton type="submit" disabled={submitting}>
              {submitting ? "Guardando..." : isEdit ? "Guardar cambios" : "Registrar"}
            </InventarioButton>
          </div>
        </form>
      </div>
    </div>
  );
}
