import { useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import {
  activarProducto,
  actualizarProducto,
  crearProducto,
  desactivarProducto,
  getComercios,
  getProductoPorId,
  getProductos,
} from "../api/productosApi";

const CONFIRM_COLOR = "#16223f";
const DANGER_COLOR = "#d6273c";

const initialFiltros = {
  buscar: "",
  comercioId: "",
  categoria: "",
  estado: "",
};

function validarCreate(form) {
  if (!form.nombre?.trim() || !form.sku?.trim() || !form.categoria?.trim() || !form.comercioId) {
    return "Completá nombre, SKU, categoría y comercio para registrar el producto.";
  }

  return null;
}

function validarUpdate(form) {
  if (!form.nombre?.trim() || !form.categoria?.trim()) {
    return "Completá nombre y categoría para actualizar el producto.";
  }

  return null;
}

function toCreatePayload(form) {
  return {
    sku: form.sku.trim(),
    nombre: form.nombre.trim(),
    descripcion: form.descripcion?.trim() || null,
    categoria: form.categoria.trim(),
    comercioId: Number(form.comercioId),
  };
}

function toUpdatePayload(form) {
  return {
    nombre: form.nombre.trim(),
    descripcion: form.descripcion?.trim() || null,
    categoria: form.categoria.trim(),
  };
}

export function useProductos() {
  const [productos, setProductos] = useState([]);
  const [todosLosProductos, setTodosLosProductos] = useState([]);
  const [comercios, setComercios] = useState([]);
  const [filtros, setFiltros] = useState(initialFiltros);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [detalleProducto, setDetalleProducto] = useState(null);

  async function cargarResumen() {
    const productosData = await getProductos();
    setTodosLosProductos(productosData ?? []);
  }

  async function cargarProductos(filtrosActuales = filtros) {
    const productosData = await getProductos(filtrosActuales);
    setProductos(productosData ?? []);
  }

  useEffect(() => {
    async function cargarDatosIniciales() {
      try {
        const [comerciosData, productosData] = await Promise.all([
          getComercios(),
          getProductos(),
        ]);
        setComercios(comerciosData ?? []);
        setTodosLosProductos(productosData ?? []);
      } catch (err) {
        Swal.fire({
          icon: "error",
          title: "No se pudieron cargar los datos",
          text: err.message,
          confirmButtonColor: CONFIRM_COLOR,
        });
      }
    }

    cargarDatosIniciales();
  }, []);

  useEffect(() => {
    async function cargarProductosFiltrados() {
      setLoading(true);

      try {
        const productosData = await getProductos(filtros);
        setProductos(productosData ?? []);
      } catch (err) {
        Swal.fire({
          icon: "error",
          title: "No se pudieron cargar los productos",
          text: err.message,
          confirmButtonColor: CONFIRM_COLOR,
        });
      } finally {
        setLoading(false);
      }
    }

    cargarProductosFiltrados();
  }, [filtros]);

  const categorias = useMemo(() => {
    return [...new Set(todosLosProductos.map((producto) => producto.categoria).filter(Boolean))]
      .sort((a, b) => a.localeCompare(b));
  }, [todosLosProductos]);

  const kpis = useMemo(() => {
    const activos = todosLosProductos.filter((producto) => producto.estado === "ACTIVO").length;
    const inactivos = todosLosProductos.filter((producto) => producto.estado === "INACTIVO").length;

    return {
      total: todosLosProductos.length,
      activos,
      inactivos,
    };
  }, [todosLosProductos]);

  function actualizarFiltro(nombre, valor) {
    setFiltros((prev) => ({
      ...prev,
      [nombre]: valor,
    }));
  }

  function limpiarFiltros() {
    setFiltros(initialFiltros);
  }

  async function refrescar() {
    await Promise.all([cargarResumen(), cargarProductos()]);
  }

  async function registrarProducto(form) {
    const errorValidacion = validarCreate(form);
    if (errorValidacion) {
      Swal.fire({
        icon: "warning",
        title: "Faltan datos",
        text: errorValidacion,
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    }

    setSubmitting(true);
    try {
      await crearProducto(toCreatePayload(form));
      await refrescar();

      Swal.fire({
        icon: "success",
        title: "Producto registrado",
        timer: 1400,
        showConfirmButton: false,
      });

      return true;
    } catch (err) {
      Swal.fire({
        icon: err.status === 409 ? "warning" : "error",
        title: err.status === 409 ? "SKU duplicado" : "No se pudo registrar el producto",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    } finally {
      setSubmitting(false);
    }
  }

  async function editarProducto(producto, form) {
    const errorValidacion = validarUpdate(form);
    if (errorValidacion) {
      Swal.fire({
        icon: "warning",
        title: "Faltan datos",
        text: errorValidacion,
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    }

    setSubmitting(true);
    try {
      await actualizarProducto(producto.id, toUpdatePayload(form));
      await refrescar();

      Swal.fire({
        icon: "success",
        title: "Producto actualizado",
        timer: 1400,
        showConfirmButton: false,
      });

      return true;
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo actualizar el producto",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    } finally {
      setSubmitting(false);
    }
  }

  async function verDetalle(producto) {
    try {
      const detalle = await getProductoPorId(producto.id);
      setDetalleProducto(detalle);
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo cargar el detalle",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    }
  }

  async function cambiarEstado(producto) {
    const esActivo = producto.estado === "ACTIVO";
    const accion = esActivo ? "desactivar" : "activar";
    const accionExitosa = esActivo ? "desactivado" : "activado";

    const confirmacion = await Swal.fire({
      icon: "warning",
      title: esActivo ? "¿Desactivar producto?" : "¿Activar producto?",
      text: esActivo
        ? "El producto quedará inactivo, pero seguirá registrado en el sistema."
        : "El producto volverá a estar activo para la operación.",
      showCancelButton: true,
      confirmButtonText: esActivo ? "Desactivar" : "Activar",
      cancelButtonText: "Cancelar",
      confirmButtonColor: esActivo ? DANGER_COLOR : CONFIRM_COLOR,
      cancelButtonColor: CONFIRM_COLOR,
    });

    if (!confirmacion.isConfirmed) return;

    try {
      if (esActivo) {
        await desactivarProducto(producto.id);
      } else {
        await activarProducto(producto.id);
      }

      await refrescar();

      Swal.fire({
        icon: "success",
        title: `Producto ${accionExitosa}`,
        timer: 1400,
        showConfirmButton: false,
      });
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: `No se pudo ${accion} el producto`,
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    }
  }

  return {
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
  };
}
