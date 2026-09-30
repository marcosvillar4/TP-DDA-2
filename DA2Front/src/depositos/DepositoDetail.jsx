import {
  ArrowLeft,
  Boxes,
  Building2,
  MapPin,
  Package,
  Pencil,
  Warehouse,
} from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  getComercios,
  getDepositoPorId,
  getItemsInventarioPorDeposito,
  updateDeposito,
} from "../../api/depositosApi";
import { getProductos } from "../../api/productosApi";
import "./styles/DepositoDetail.css";

async function obtenerDatosDetalle(id) {
  const [depositoData, comerciosData] = await Promise.all([
    getDepositoPorId(id),
    getComercios(),
  ]);

  let itemsData = [];
  let inventarioError = "";
  let productosData = [];
  let productosError = "";

  try {
    itemsData = (await getItemsInventarioPorDeposito(id)) ?? [];
  } catch (err) {
    inventarioError =
      err.message || "No se pudo cargar el inventario del depósito.";
  }

  try {
    productosData = (await getProductos()) ?? [];
  } catch (err) {
    productosError = err.message || "No se pudo cargar el catálogo de productos.";
  }

  return {
    depositoData,
    comerciosData: comerciosData ?? [],
    itemsData,
    inventarioError,
    productosData,
    productosError,
  };
}

export default function DepositoDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [deposito, setDeposito] = useState(null);
  const [comercios, setComercios] = useState([]);
  const [items, setItems] = useState([]);
  const [productos, setProductos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [inventarioError, setInventarioError] = useState("");
  const [productosError, setProductosError] = useState("");
  const [editOpen, setEditOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  const aplicarDatosDetalle = useCallback((datos) => {
    setDeposito(datos.depositoData);
    setComercios(datos.comerciosData);
    setItems(datos.itemsData);
    setInventarioError(datos.inventarioError);
    setProductos(datos.productosData);
    setProductosError(datos.productosError);
  }, []);

  const cargarDetalle = useCallback(async () => {
    setLoading(true);
    setError("");
    setInventarioError("");
    setProductosError("");

    try {
      const datos = await obtenerDatosDetalle(id);
      aplicarDatosDetalle(datos);
    } catch (err) {
      setError(err.message || "No se pudo cargar el depósito.");
    } finally {
      setLoading(false);
    }
  }, [aplicarDatosDetalle, id]);

  useEffect(() => {
    let activo = true;

    async function cargarDatosIniciales() {
      try {
        const datos = await obtenerDatosDetalle(id);

        if (!activo) return;

        aplicarDatosDetalle(datos);
      } catch (err) {
        if (!activo) return;

        setError(err.message || "No se pudo cargar el depósito.");
      } finally {
        if (activo) {
          setLoading(false);
        }
      }
    }

    cargarDatosIniciales();

    return () => {
      activo = false;
    };
  }, [aplicarDatosDetalle, id]);

  const comercioNombre = useMemo(() => {
    if (!deposito?.comercioId) return null;
    return comercios.find((comercio) => comercio.id === deposito.comercioId)
      ?.nombreComercial;
  }, [deposito, comercios]);

  const productosPorId = useMemo(() => {
    return new Map(productos.map((producto) => [producto.id, producto]));
  }, [productos]);

  const productosAlmacenados = useMemo(() => {
    const productosAgrupados = new Map();

    items.forEach((item) => {
      const productoId = item.productoId;
      const cantidad = Number(item.cantidad) || 0;
      const producto = productosPorId.get(productoId);
      const existente = productosAgrupados.get(productoId);

      if (existente) {
        existente.cantidad += cantidad;
        return;
      }

      productosAgrupados.set(productoId, {
        productoId,
        sku: producto?.sku || "—",
        nombre: producto?.nombre || `Producto #${productoId}`,
        categoria: producto?.categoria || "—",
        cantidad,
        resuelto: Boolean(producto),
      });
    });

    return Array.from(productosAgrupados.values()).sort((a, b) =>
      a.nombre.localeCompare(b.nombre)
    );
  }, [items, productosPorId]);

  const metricasStock = useMemo(() => {
    const productosDiferentes = new Set(
      items.map((item) => item.productoId).filter(Boolean)
    ).size;
    const unidadesAlmacenadas = items.reduce(
      (total, item) => total + (Number(item.cantidad) || 0),
      0
    );

    return {
      productosDiferentes,
      unidadesAlmacenadas,
    };
  }, [items]);

  const guardarDeposito = async (formulario) => {
    const nombre = formulario.nombre.trim();
    const direccion = formulario.direccion.trim();

    if (!nombre || !direccion) {
      return {
        ok: false,
        message: "Completá el nombre y la dirección del depósito.",
      };
    }

    setSaving(true);
    setSuccessMessage("");

    try {
      const depositoActualizado = await updateDeposito(deposito.id, {
        nombre,
        direccion,
      });

      setDeposito(depositoActualizado);
      setEditOpen(false);
      setSuccessMessage("Depósito actualizado correctamente.");
      return { ok: true };
    } catch (err) {
      return {
        ok: false,
        message: err.message || "No se pudo actualizar el depósito.",
      };
    } finally {
      setSaving(false);
    }
  };

  return (
    <section className="deposito-detail-page">
      <button
        type="button"
        className="deposito-back-button"
        onClick={() => navigate("/depositos")}
      >
        <ArrowLeft size={16} />
        Volver a Depósitos
      </button>

      {loading ? (
        <div className="deposito-detail-state">Cargando depósito...</div>
      ) : error ? (
        <div className="deposito-detail-state deposito-detail-error">{error}</div>
      ) : !deposito ? (
        <div className="deposito-detail-state">No se encontró el depósito.</div>
      ) : (
        <>
          <div className="deposito-detail-header">
            <div className="deposito-detail-title">
              <div className="deposito-detail-icon">
                <Warehouse size={28} />
              </div>
              <div>
                <h1>{deposito.nombre}</h1>
                <p>Información operativa y stock registrado en este depósito</p>
              </div>
            </div>
            <button
              type="button"
              className="deposito-edit-button"
              onClick={() => {
                setSuccessMessage("");
                setEditOpen(true);
              }}
            >
              <Pencil size={16} />
              Editar depósito
            </button>
          </div>

          {successMessage && (
            <div className="deposito-detail-success">{successMessage}</div>
          )}

          <div className="deposito-detail-summary">
            <MetricCard
              icon={Package}
              label="Productos diferentes"
              value={metricasStock.productosDiferentes}
            />
            <MetricCard
              icon={Boxes}
              label="Unidades almacenadas"
              value={metricasStock.unidadesAlmacenadas}
            />
          </div>

          <div className="deposito-detail-layout">
            <div className="deposito-detail-card">
              <h2>Datos del depósito</h2>

              <div className="deposito-detail-row">
                <span>
                  <MapPin size={16} />
                  Dirección
                </span>
                <strong>{deposito.direccion}</strong>
              </div>

              <div className="deposito-detail-row">
                <span>
                  <Building2 size={16} />
                  Comercio asociado
                </span>
                <strong>{comercioNombre || "Sin comercio asociado"}</strong>
              </div>
            </div>

            <div className="deposito-detail-card deposito-inventory-card">
              <div className="deposito-inventory-header">
                <div>
                  <h2>Productos almacenados</h2>
                  <p>Unidades registradas en Inventario para este depósito.</p>
                </div>
                {(inventarioError || productosError) && (
                  <button
                    type="button"
                    className="deposito-retry-button"
                    onClick={cargarDetalle}
                  >
                    Reintentar
                  </button>
                )}
              </div>

              {inventarioError ? (
                <div className="deposito-detail-state deposito-detail-error">
                  {inventarioError}
                </div>
              ) : items.length === 0 ? (
                <div className="deposito-detail-empty">
                  No hay productos almacenados en este depósito.
                </div>
              ) : (
                <>
                  {productosError && (
                    <div className="deposito-detail-warning">
                      {productosError} Se muestran los productos por ID.
                    </div>
                  )}

                  <div className="deposito-products-table-wrapper">
                    <table className="deposito-products-table">
                      <thead>
                        <tr>
                          <th>SKU</th>
                          <th>Producto</th>
                          <th>Categoría</th>
                          <th>Cantidad</th>
                        </tr>
                      </thead>
                      <tbody>
                        {productosAlmacenados.map((item) => (
                          <tr key={item.productoId}>
                            <td className="deposito-product-sku">{item.sku}</td>
                            <td>
                              {item.nombre}
                              {!item.resuelto && (
                                <small className="deposito-product-unresolved">
                                  No se pudo resolver el producto del catálogo
                                </small>
                              )}
                            </td>
                            <td>{item.categoria}</td>
                            <td className="deposito-product-quantity">
                              {item.cantidad}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </>
              )}
            </div>
          </div>

          {editOpen && (
            <DepositoEditModal
              deposito={deposito}
              saving={saving}
              onCancel={() => setEditOpen(false)}
              onSubmit={guardarDeposito}
            />
          )}
        </>
      )}
    </section>
  );
}

function MetricCard({ icon: Icon, label, value }) {
  return (
    <div className="deposito-metric-card">
      <span className="deposito-metric-icon">
        <Icon size={18} />
      </span>
      <span className="deposito-metric-value">{value}</span>
      <span className="deposito-metric-label">{label}</span>
    </div>
  );
}

function DepositoEditModal({ deposito, saving, onCancel, onSubmit }) {
  const [formulario, setFormulario] = useState({
    nombre: deposito.nombre || "",
    direccion: deposito.direccion || "",
  });
  const [error, setError] = useState("");

  const actualizarCampo = (event) => {
    const { name, value } = event.target;

    setFormulario((actual) => ({
      ...actual,
      [name]: value,
    }));
  };

  const guardar = async (event) => {
    event.preventDefault();
    setError("");

    const resultado = await onSubmit(formulario);

    if (!resultado.ok) {
      setError(resultado.message);
    }
  };

  return (
    <div className="deposito-modal-backdrop" role="presentation">
      <div className="deposito-edit-modal" role="dialog" aria-modal="true">
        <div className="deposito-edit-modal-header">
          <div>
            <h2>Editar depósito</h2>
            <p>Modificá únicamente los datos generales del depósito.</p>
          </div>
          <button
            type="button"
            className="deposito-modal-close"
            onClick={onCancel}
            disabled={saving}
            aria-label="Cerrar edición"
          >
            ×
          </button>
        </div>

        <form className="deposito-edit-form" onSubmit={guardar}>
          <label>
            Nombre
            <input
              type="text"
              name="nombre"
              value={formulario.nombre}
              onChange={actualizarCampo}
              disabled={saving}
              required
            />
          </label>

          <label>
            Dirección
            <input
              type="text"
              name="direccion"
              value={formulario.direccion}
              onChange={actualizarCampo}
              disabled={saving}
              required
            />
          </label>

          {error && <div className="deposito-edit-error">{error}</div>}

          <div className="deposito-edit-actions">
            <button
              type="button"
              className="deposito-edit-secondary"
              onClick={onCancel}
              disabled={saving}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="deposito-edit-primary"
              disabled={saving}
            >
              {saving ? "Guardando..." : "Guardar cambios"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
