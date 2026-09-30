import { useEffect, useMemo, useState } from "react";
import { getComercios, getDepositos } from "../api/depositosApi";

async function obtenerDatosDepositos() {
  const [depositosData, comerciosData] = await Promise.all([
    getDepositos(),
    getComercios(),
  ]);

  return {
    depositosData: depositosData ?? [],
    comerciosData: comerciosData ?? [],
  };
}

export function useDepositos() {
  const [depositos, setDepositos] = useState([]);
  const [comercios, setComercios] = useState([]);
  const [busqueda, setBusqueda] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function cargarDepositos() {
    setLoading(true);
    setError("");

    try {
      const { depositosData, comerciosData } = await obtenerDatosDepositos();

      setDepositos(depositosData);
      setComercios(comerciosData);
    } catch (err) {
      setError(err.message || "No se pudieron cargar los depositos.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    let activo = true;

    async function cargarDatosIniciales() {
      try {
        const { depositosData, comerciosData } = await obtenerDatosDepositos();

        if (!activo) return;

        setDepositos(depositosData);
        setComercios(comerciosData);
      } catch (err) {
        if (!activo) return;

        setError(err.message || "No se pudieron cargar los depositos.");
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
  }, []);

  const comerciosPorId = useMemo(() => {
    return new Map(comercios.map((comercio) => [comercio.id, comercio]));
  }, [comercios]);

  const depositosConComercio = useMemo(() => {
    return depositos.map((deposito) => {
      const comercio = deposito.comercioId
        ? comerciosPorId.get(deposito.comercioId)
        : null;

      return {
        ...deposito,
        comercioNombre: comercio?.nombreComercial || null,
      };
    });
  }, [depositos, comerciosPorId]);

  const depositosFiltrados = useMemo(() => {
    const termino = busqueda.trim().toLowerCase();

    if (!termino) {
      return depositosConComercio;
    }

    return depositosConComercio.filter((deposito) => {
      return [
        deposito.nombre,
        deposito.direccion,
        deposito.comercioNombre,
      ]
        .filter(Boolean)
        .some((valor) => valor.toLowerCase().includes(termino));
    });
  }, [busqueda, depositosConComercio]);

  const resumen = useMemo(() => {
    const asociados = depositos.filter((deposito) => Boolean(deposito.comercioId)).length;

    return {
      total: depositos.length,
      asociados,
      sinComercio: depositos.length - asociados,
    };
  }, [depositos]);

  return {
    depositos,
    depositosFiltrados,
    busqueda,
    loading,
    error,
    resumen,
    setBusqueda,
    refrescar: cargarDepositos,
  };
}
