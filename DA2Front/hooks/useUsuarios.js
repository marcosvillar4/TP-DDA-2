import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchUsuarios } from "../api/usuariosApi";

const POR_PAGINA = 10;
const ESTADOS = ["EN_EVALUACION", "VALIDADO", "BLOQUEADO", "RECHAZADO"];
// En la vista "Todos", los pendientes de evaluación van primero.
const ORDEN_ESTADO = { EN_EVALUACION: 0, VALIDADO: 1, BLOQUEADO: 2, RECHAZADO: 3 };

/**
 * Los filtros viven en la URL (?estado=EN_EVALUACION&rol=COMERCIO&q=ruiz&page=2).
 * Eso permite enlazar directo a "pendientes" desde el sidebar o un dashboard,
 * y que el botón "Volver" desde el detalle conserve filtros y página.
 */
export function useUsuarios() {
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [searchParams, setSearchParams] = useSearchParams();

  const busqueda = searchParams.get("q") ?? "";
  const filtroRol = searchParams.get("rol") ?? "TODOS";
  const estadoParam = searchParams.get("estado");
  const filtroEstado = ESTADOS.includes(estadoParam) ? estadoParam : "TODOS";
  const paginaParam = Number.parseInt(searchParams.get("page"), 10);
  const pagina = Number.isNaN(paginaParam) || paginaParam < 1 ? 1 : paginaParam;

  const cargarUsuarios = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setUsuarios(await fetchUsuarios());
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargarUsuarios();
  }, [cargarUsuarios]);

  const actualizarParams = useCallback(
    (cambios, { resetPagina = true } = {}) => {
      setSearchParams(
        (prev) => {
          const next = new URLSearchParams(prev);
          for (const [clave, valor] of Object.entries(cambios)) {
            const vacio =
              valor == null ||
              valor === "" ||
              valor === "TODOS" ||
              (clave === "page" && Number(valor) === 1);
            if (vacio) next.delete(clave);
            else next.set(clave, String(valor));
          }
          if (resetPagina) next.delete("page");
          return next;
        },
        { replace: true }
      );
    },
    [setSearchParams]
  );

  const stats = useMemo(
    () => ({
      total: usuarios.length,
      enEvaluacion: usuarios.filter((u) => u.estado === "EN_EVALUACION").length,
      validados: usuarios.filter((u) => u.estado === "VALIDADO").length,
      bloqueados: usuarios.filter((u) => u.estado === "BLOQUEADO").length,
      rechazados: usuarios.filter((u) => u.estado === "RECHAZADO").length,
    }),
    [usuarios]
  );

  const filtrados = useMemo(() => {
    const q = busqueda.trim().toLowerCase();
    return usuarios
      .filter((u) => {
        const coincideBusqueda =
          q === "" ||
          `${u.nombre} ${u.apellido}`.toLowerCase().includes(q) ||
          (u.email ?? "").toLowerCase().includes(q);
        const coincideRol = filtroRol === "TODOS" || u.rol === filtroRol;
        const coincideEstado = filtroEstado === "TODOS" || u.estado === filtroEstado;
        return coincideBusqueda && coincideRol && coincideEstado;
      })
      .sort(
        (a, b) =>
          (ORDEN_ESTADO[a.estado] ?? 9) - (ORDEN_ESTADO[b.estado] ?? 9) ||
          `${a.apellido} ${a.nombre}`.localeCompare(`${b.apellido} ${b.nombre}`, "es")
      );
  }, [usuarios, busqueda, filtroRol, filtroEstado]);

  const totalPaginas = Math.max(1, Math.ceil(filtrados.length / POR_PAGINA));
  const paginaActual = Math.min(pagina, totalPaginas);
  const usuariosPagina = filtrados.slice(
    (paginaActual - 1) * POR_PAGINA,
    paginaActual * POR_PAGINA
  );

  // IDs de los pendientes dentro de lo que se está viendo: es la "cola de revisión"
  // que se le pasa al detalle para poder avanzar de uno en uno.
  const colaPendientes = useMemo(
    () => filtrados.filter((u) => u.estado === "EN_EVALUACION").map((u) => u.id),
    [filtrados]
  );

  return {
    usuarios: usuariosPagina,
    totalFiltrados: filtrados.length,
    stats,
    loading,
    error,
    recargar: cargarUsuarios,
    colaPendientes,

    busqueda,
    setBusqueda: (v) => actualizarParams({ q: v }),
    filtroRol,
    setFiltroRol: (v) => actualizarParams({ rol: v }),
    filtroEstado,
    setFiltroEstado: (v) => actualizarParams({ estado: v }),
    hayFiltros: busqueda !== "" || filtroRol !== "TODOS" || filtroEstado !== "TODOS",
    limpiarFiltros: () => setSearchParams({}, { replace: true }),

    pagina: paginaActual,
    totalPaginas,
    porPagina: POR_PAGINA,
    setPagina: (n) => actualizarParams({ page: n }, { resetPagina: false }),
  };
}