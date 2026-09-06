import { useEffect, useState } from "react";
import Swal from "sweetalert2";
import {
  getComercios,
  getProductos,
  getDepositosPorComercio,
  getInventarioPorComercio,
  crearInventario,
  eliminarInventario,
  getItemsPorInventario,
  crearItemInventario,
  actualizarCantidadItem,
  eliminarItemInventario,
} from "../api/inventarioApi";

const CONFIRM_COLOR = "#16223f";
const DANGER_COLOR = "#d6273c";

export function useInventario() {
  const [comercios, setComercios] = useState([]);
  const [productos, setProductos] = useState([]);
  const [depositos, setDepositos] = useState([]);
  const [comercioId, setComercioId] = useState("");
  const [inventario, setInventario] = useState(null);
  const [items, setItems] = useState([]);

  const [loadingComercios, setLoadingComercios] = useState(true);
  const [loadingInventario, setLoadingInventario] = useState(false);
  const [creatingInventario, setCreatingInventario] = useState(false);
  const [submittingItem, setSubmittingItem] = useState(false);

  useEffect(() => {
    async function cargarDatosIniciales() {
      try {
        const [comerciosData, productosData] = await Promise.all([
          getComercios(),
          getProductos(),
        ]);
        setComercios(comerciosData ?? []);
        setProductos(productosData ?? []);
      } catch (err) {
        Swal.fire({
          icon: "error",
          title: "No se pudieron cargar los datos",
          text: err.message,
          confirmButtonColor: CONFIRM_COLOR,
        });
      } finally {
        setLoadingComercios(false);
      }
    }

    cargarDatosIniciales();
  }, []);

  async function cargarInventario(idComercio) {
    setLoadingInventario(true);
    setInventario(null);
    setItems([]);
    setDepositos([]);

    try {
      const inventarioEncontrado = await getInventarioPorComercio(idComercio);
      setInventario(inventarioEncontrado);

      if (inventarioEncontrado) {
        const itemsData = await getItemsPorInventario(inventarioEncontrado.id);
        setItems(itemsData ?? []);
      }
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo cargar el inventario",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    } finally {
      setLoadingInventario(false);
    }

    try {
      const depositosData = await getDepositosPorComercio(idComercio);
      setDepositos(depositosData ?? []);
    } catch {
      // El usuario logueado puede no tener el rol DEPOSITO, requerido
      // por /deposito/**. Se deja la lista vacía sin bloquear la pantalla.
      setDepositos([]);
    }
  }

  function handleSelectComercio(idComercio) {
    setComercioId(idComercio);

    if (idComercio) {
      cargarInventario(idComercio);
    } else {
      setInventario(null);
      setItems([]);
      setDepositos([]);
    }
  }

  async function handleCrearInventario() {
    setCreatingInventario(true);
    try {
      const nuevoInventario = await crearInventario(Number(comercioId));
      setInventario(nuevoInventario);
      setItems([]);

      Swal.fire({
        icon: "success",
        title: "Inventario creado",
        timer: 1400,
        showConfirmButton: false,
      });
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo crear el inventario",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    } finally {
      setCreatingInventario(false);
    }
  }

  async function handleEliminarInventario() {
    if (!inventario) return;

    const confirmacion = await Swal.fire({
      icon: "warning",
      title: "¿Eliminar inventario?",
      text: "Se eliminarán también todos sus ítems.",
      showCancelButton: true,
      confirmButtonText: "Eliminar",
      cancelButtonText: "Cancelar",
      confirmButtonColor: DANGER_COLOR,
      cancelButtonColor: CONFIRM_COLOR,
    });

    if (!confirmacion.isConfirmed) return;

    try {
      await eliminarInventario(inventario.id);
      setInventario(null);
      setItems([]);

      Swal.fire({
        icon: "success",
        title: "Inventario eliminado",
        timer: 1400,
        showConfirmButton: false,
      });
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo eliminar el inventario",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    }
  }

  async function handleAgregarItem({ productoId, depositoId, cantidad }) {
    if (!inventario) return false;

    if (!productoId || !depositoId || !cantidad) {
      Swal.fire({
        icon: "warning",
        title: "Faltan datos",
        text: "Completá producto, depósito y cantidad para continuar.",
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    }

    setSubmittingItem(true);
    try {
      await crearItemInventario({
        inventarioId: inventario.id,
        productoId: Number(productoId),
        depositoId: Number(depositoId),
        cantidad: Number(cantidad),
      });

      const itemsActualizados = await getItemsPorInventario(inventario.id);
      setItems(itemsActualizados ?? []);

      Swal.fire({
        icon: "success",
        title: "Ítem agregado",
        timer: 1300,
        showConfirmButton: false,
      });

      return true;
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo agregar el ítem",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    } finally {
      setSubmittingItem(false);
    }
  }

  async function handleActualizarCantidad(itemId, cantidad) {
    try {
      const itemActualizado = await actualizarCantidadItem(
        itemId,
        Number(cantidad)
      );
      setItems((prev) =>
        prev.map((item) => (item.id === itemId ? itemActualizado : item))
      );
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo actualizar la cantidad",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    }
  }

  async function handleEliminarItem(itemId) {
    const confirmacion = await Swal.fire({
      icon: "warning",
      title: "¿Eliminar ítem?",
      showCancelButton: true,
      confirmButtonText: "Eliminar",
      cancelButtonText: "Cancelar",
      confirmButtonColor: DANGER_COLOR,
      cancelButtonColor: CONFIRM_COLOR,
    });

    if (!confirmacion.isConfirmed) return;

    try {
      await eliminarItemInventario(itemId);
      setItems((prev) => prev.filter((item) => item.id !== itemId));
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo eliminar el ítem",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    }
  }

  return {
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
  };
}