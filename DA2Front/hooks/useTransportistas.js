import { useCallback, useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import {
  activarTransportista,
  actualizarTransportista,
  cambiarEstadoTransportista,
  crearTransportista,
  desactivarTransportista,
  getTransportistas,
} from "../api/transportistasApi";

export function useTransportistas() {
  const [transportistas, setTransportistas] = useState([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [buscar, setBuscar] = useState("");
  const [estado, setEstado] = useState("");

  const cargar = useCallback(async (mostrarCarga = true) => {
    if (mostrarCarga) setLoading(true);
    setError("");
    try {
      const data = await getTransportistas();
      setTransportistas(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    getTransportistas()
      .then((data) => setTransportistas(Array.isArray(data) ? data : []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false));
  }, []);

  const filtrados = useMemo(() => {
    const termino = buscar.trim().toLowerCase();
    return transportistas.filter((item) =>
      (!termino || item.empresa?.toLowerCase().includes(termino)) &&
      (!estado || item.estado === estado)
    );
  }, [transportistas, buscar, estado]);

  const kpis = useMemo(() => ({
    total: transportistas.length,
    disponibles: transportistas.filter((item) => item.activo && item.estado === "DISPONIBLE").length,
    enServicio: transportistas.filter((item) => item.activo && item.estado === "EN_SERVICIO").length,
    noDisponibles: transportistas.filter((item) => !item.activo || item.estado === "NO_DISPONIBLE").length,
  }), [transportistas]);

  async function guardar(payload, id = null) {
    setSaving(true);
    try {
      if (id) await actualizarTransportista(id, payload);
      else await crearTransportista(payload);
      await cargar();
      await Swal.fire({ icon: "success", title: id ? "Transportista actualizado" : "Transportista creado", timer: 1400, showConfirmButton: false });
      return true;
    } catch (err) {
      await Swal.fire({ icon: "error", title: "No se pudo guardar", text: err.message, confirmButtonColor: "#16223f" });
      return false;
    } finally {
      setSaving(false);
    }
  }

  async function ejecutar(accion, titulo, texto) {
    const confirmacion = await Swal.fire({
      icon: "question", title: titulo, text: texto, showCancelButton: true,
      confirmButtonColor: "#16223f", cancelButtonColor: "#64748b",
      confirmButtonText: "Confirmar", cancelButtonText: "Cancelar",
    });
    if (!confirmacion.isConfirmed) return false;
    setSaving(true);
    try {
      await accion();
      await cargar();
      return true;
    } catch (err) {
      await Swal.fire({ icon: "error", title: "No se pudo actualizar", text: err.message, confirmButtonColor: "#16223f" });
      return false;
    } finally {
      setSaving(false);
    }
  }

  return {
    transportistas: filtrados, todos: transportistas, loading, saving, error, buscar, estado, kpis,
    setBuscar, setEstado, guardar,
    cambiarEstado: (id, value) => ejecutar(() => cambiarEstadoTransportista(id, value), "Cambiar estado", "¿Querés actualizar el estado del transportista?"),
    cambiarActivo: (item) => ejecutar(
      () => (item.activo ? desactivarTransportista(item.id) : activarTransportista(item.id)),
      item.activo ? "Desactivar transportista" : "Activar transportista",
      `¿Querés ${item.activo ? "desactivar" : "activar"} a ${item.empresa}?`
    ),
  };
}
