import { useCallback, useEffect, useState } from "react";
import { getMiDeposito } from "../api/depositosApi";
import { getItemsPorDeposito } from "../api/inventarioApi";

/**
 * Inventario del depósito del usuario autenticado (rol DEPOSITO).
 * Es solo lectura: el stock lo carga y administra el comercio.
 */
export function useInventarioDeposito() {
  const [deposito, setDeposito] = useState(null);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [sinDeposito, setSinDeposito] = useState(false);
  const [error, setError] = useState("");

  const cargar = useCallback(async () => {
    setLoading(true);
    setError("");
    setSinDeposito(false);

    try {
      const miDeposito = await getMiDeposito();
      setDeposito(miDeposito);

      const data = await getItemsPorDeposito(miDeposito.id);
      setItems(data ?? []);
    } catch (err) {
      setItems([]);

      // 404 de /depositos/me = la cuenta todavía no tiene un depósito asociado.
      if (err.status === 404) {
        setDeposito(null);
        setSinDeposito(true);
      } else {
        setError(err.message);
      }
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    cargar();
  }, [cargar]);

  return { deposito, items, loading, sinDeposito, error, recargar: cargar };
}
