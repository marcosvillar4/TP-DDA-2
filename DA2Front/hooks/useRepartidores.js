import { useCallback, useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import {
  activarRepartidor,
  actualizarRepartidor,
  asignarPedido,
  cambiarEstadoRepartidor,
  crearRepartidor,
  desactivarRepartidor,
  getHistorialRepartidor,
  getPedidosAsignables,
  getRepartidorPorId,
  getRepartidores,
  getUsuariosRepartidoresDisponibles,
} from "../api/repartidoresApi";

const EMPTY_FILTERS = {
  buscar: "",
  estado: "",
  zona: "",
};

export function useRepartidores() {
  const [repartidores, setRepartidores] = useState([]);
  const [usuariosDisponibles, setUsuariosDisponibles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [filters, setFilters] = useState(EMPTY_FILTERS);

  const cargarRepartidores = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getRepartidores();
      setRepartidores(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  const cargarUsuariosDisponibles = useCallback(async () => {
    try {
      const data = await getUsuariosRepartidoresDisponibles();
      setUsuariosDisponibles(Array.isArray(data) ? data : []);
    } catch {
      setUsuariosDisponibles([]);
    }
  }, []);

  useEffect(() => {
    cargarRepartidores();
    cargarUsuariosDisponibles();
  }, [cargarRepartidores, cargarUsuariosDisponibles]);

  const repartidoresFiltrados = useMemo(() => {
    const buscar = filters.buscar.trim().toLowerCase();

    return repartidores.filter((repartidor) => {
      const nombre = repartidor.nombreCompleto?.toLowerCase() || "";
      const patente = repartidor.patente?.toLowerCase() || "";
      const vehiculo = repartidor.tipoVehiculo?.toLowerCase() || "";
      const zona = repartidor.zona || "";

      const matchBuscar =
        !buscar ||
        nombre.includes(buscar) ||
        patente.includes(buscar) ||
        vehiculo.includes(buscar);

      const matchEstado = !filters.estado || repartidor.estado === filters.estado;
      const matchZona = !filters.zona || zona === filters.zona;

      return matchBuscar && matchEstado && matchZona;
    });
  }, [repartidores, filters]);

  const zonas = useMemo(
    () => [...new Set(repartidores.map((r) => r.zona).filter(Boolean))].sort(),
    [repartidores]
  );

  const kpis = useMemo(
    () => ({
      total: repartidores.length,
      disponibles: repartidores.filter((r) => r.activo && r.estado === "DISPONIBLE").length,
      enEntrega: repartidores.filter((r) => r.activo && r.estado === "EN_ENTREGA").length,
      noDisponibles: repartidores.filter((r) => !r.activo || r.estado === "NO_DISPONIBLE").length,
    }),
    [repartidores]
  );

  function updateFilter(name, value) {
    setFilters((current) => ({ ...current, [name]: value }));
  }

  function resetFilters() {
    setFilters(EMPTY_FILTERS);
  }

  async function guardarRepartidor(payload, repartidorId = null) {
    setSaving(true);
    try {
      if (repartidorId) {
        await actualizarRepartidor(repartidorId, payload);
        await Swal.fire({
          icon: "success",
          title: "Repartidor actualizado",
          timer: 1400,
          showConfirmButton: false,
        });
      } else {
        await crearRepartidor(payload);
        await Swal.fire({
          icon: "success",
          title: "Repartidor creado",
          timer: 1400,
          showConfirmButton: false,
        });
      }

      await cargarRepartidores();
      await cargarUsuariosDisponibles();
      return true;
    } catch (err) {
      await Swal.fire({
        icon: "error",
        title: "No se pudo guardar",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setSaving(false);
    }
  }

  async function cambiarEstado(id, estado) {
    setSaving(true);
    try {
      await cambiarEstadoRepartidor(id, estado);
      await cargarRepartidores();
      return true;
    } catch (err) {
      await Swal.fire({
        icon: "error",
        title: "No se pudo cambiar el estado",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setSaving(false);
    }
  }

  async function cambiarActivo(repartidor, activar) {
    const accion = activar ? "activar" : "desactivar";
    const result = await Swal.fire({
      icon: "question",
      title: `${activar ? "Activar" : "Desactivar"} repartidor`,
      text: `¿Querés ${accion} a ${repartidor.nombreCompleto}?`,
      showCancelButton: true,
      confirmButtonColor: "#16223f",
      cancelButtonColor: "#64748b",
      confirmButtonText: activar ? "Activar" : "Desactivar",
      cancelButtonText: "Cancelar",
    });

    if (!result.isConfirmed) {
      return false;
    }

    setSaving(true);
    try {
      if (activar) {
        await activarRepartidor(repartidor.id);
      } else {
        await desactivarRepartidor(repartidor.id);
      }

      await cargarRepartidores();
      return true;
    } catch (err) {
      await Swal.fire({
        icon: "error",
        title: "No se pudo actualizar",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setSaving(false);
    }
  }

  return {
    repartidores,
    repartidoresFiltrados,
    usuariosDisponibles,
    loading,
    saving,
    error,
    filters,
    zonas,
    kpis,
    updateFilter,
    resetFilters,
    cargarRepartidores,
    cargarUsuariosDisponibles,
    guardarRepartidor,
    cambiarEstado,
    cambiarActivo,
  };
}

export function useRepartidorDetalle(id) {
  const [repartidor, setRepartidor] = useState(null);
  const [historial, setHistorial] = useState([]);
  const [pedidosAsignables, setPedidosAsignables] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const cargarDetalle = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const [repartidorData, historialData, pedidosData] = await Promise.all([
        getRepartidorPorId(id),
        getHistorialRepartidor(id),
        getPedidosAsignables(),
      ]);

      setRepartidor(repartidorData);
      setHistorial(Array.isArray(historialData) ? historialData : []);
      setPedidosAsignables(Array.isArray(pedidosData) ? pedidosData : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    cargarDetalle();
  }, [cargarDetalle]);

  async function guardarCambios(payload) {
    setSaving(true);
    try {
      await actualizarRepartidor(id, payload);
      await cargarDetalle();
      await Swal.fire({
        icon: "success",
        title: "Repartidor actualizado",
        timer: 1400,
        showConfirmButton: false,
      });
      return true;
    } catch (err) {
      await Swal.fire({
        icon: "error",
        title: "No se pudo guardar",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setSaving(false);
    }
  }

  async function asignarPedidoPendiente(pedidoId) {
    setSaving(true);
    try {
      await asignarPedido(id, pedidoId);
      await cargarDetalle();
      await Swal.fire({
        icon: "success",
        title: "Pedido asignado",
        text: "El repartidor pasó a estado En entrega.",
        confirmButtonColor: "#16223f",
      });
      return true;
    } catch (err) {
      await Swal.fire({
        icon: "error",
        title: "No se pudo asignar",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setSaving(false);
    }
  }

  async function cambiarEstadoDetalle(estado) {
    setSaving(true);
    try {
      await cambiarEstadoRepartidor(id, estado);
      await cargarDetalle();
      return true;
    } catch (err) {
      await Swal.fire({
        icon: "error",
        title: "No se pudo cambiar el estado",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setSaving(false);
    }
  }

  async function cambiarActivoDetalle(activar) {
    if (!repartidor) return false;

    const result = await Swal.fire({
      icon: "question",
      title: `${activar ? "Activar" : "Desactivar"} repartidor`,
      text: `¿Querés ${activar ? "activar" : "desactivar"} a ${repartidor.nombreCompleto}?`,
      showCancelButton: true,
      confirmButtonColor: "#16223f",
      cancelButtonColor: "#64748b",
      confirmButtonText: activar ? "Activar" : "Desactivar",
      cancelButtonText: "Cancelar",
    });

    if (!result.isConfirmed) {
      return false;
    }

    setSaving(true);
    try {
      if (activar) {
        await activarRepartidor(id);
      } else {
        await desactivarRepartidor(id);
      }
      await cargarDetalle();
      return true;
    } catch (err) {
      await Swal.fire({
        icon: "error",
        title: "No se pudo actualizar",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setSaving(false);
    }
  }

  return {
    repartidor,
    historial,
    pedidosAsignables,
    loading,
    saving,
    error,
    cargarDetalle,
    guardarCambios,
    asignarPedidoPendiente,
    cambiarEstadoDetalle,
    cambiarActivoDetalle,
  };
}
