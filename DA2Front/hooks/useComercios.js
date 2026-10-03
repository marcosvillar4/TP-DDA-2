import { useEffect, useState } from "react";
import { getComercios } from "../api/comerciosApi";

/** Listado de comercios (vista ADMIN), directo del backend. */
export function useComercios() {
  const [comercios, setComercios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelado = false;

    getComercios()
      .then((data) => {
        if (!cancelado) setComercios(data ?? []);
      })
      .catch((err) => {
        if (!cancelado) setError(err.message);
      })
      .finally(() => {
        if (!cancelado) setLoading(false);
      });

    return () => {
      cancelado = true;
    };
  }, []);

  return { comercios, loading, error };
}
