import { useEffect, useMemo, useState } from "react";
import { fetchUsuarios } from "../api/usuariosApi";

export function useUsuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [busqueda, setBusqueda] = useState("");
  const [filtroRol, setFiltroRol] = useState("TODOS");
  const [filtroEstado, setFiltroEstado] = useState("TODOS");

  async function cargarUsuarios() {
    setLoading(true);
    setError(null);
    try {
      setUsuarios(await fetchUsuarios());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    cargarUsuarios();
  }, []);

  const stats = useMemo(() => ({
    total: usuarios.length,
    validados: usuarios.filter((u) => u.estado === "VALIDADO").length,
    enEvaluacion: usuarios.filter((u) => u.estado === "EN_EVALUACION").length,
    bloqueados: usuarios.filter((u) => u.estado === "BLOQUEADO").length,
  }), [usuarios]);

  const usuariosFiltrados = useMemo(() => {
    return usuarios.filter((u) => {
      const coincideBusqueda =
        busqueda.trim() === "" ||
        `${u.nombre} ${u.apellido}`.toLowerCase().includes(busqueda.toLowerCase()) ||
        u.email.toLowerCase().includes(busqueda.toLowerCase());
      const coincideRol = filtroRol === "TODOS" || u.rol === filtroRol;
      const coincideEstado = filtroEstado === "TODOS" || u.estado === filtroEstado;
      return coincideBusqueda && coincideRol && coincideEstado;
    });
  }, [usuarios, busqueda, filtroRol, filtroEstado]);

  return {
    usuarios: usuariosFiltrados,
    stats,
    loading,
    error,
    busqueda, setBusqueda,
    filtroRol, setFiltroRol,
    filtroEstado, setFiltroEstado,
  };
}