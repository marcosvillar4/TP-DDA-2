import { useEffect, useState } from "react";
import { getComercioPorId, getMiComercio } from "../api/comerciosApi";
import { getPedidosPorComercio } from "../api/pedidosApi";
import { getDepositosPorComercio } from "../api/depositosApi";

/**
 * Carga un comercio con sus pedidos y depósitos vinculados.
 *
 * @param {string|undefined} id     - id del comercio (ruta /comercios/:id)
 * @param {boolean} propio          - true para el usuario COMERCIO: usa /comercios/me
 */
export function useComercioDetail(id, propio = false) {
  const [comercio, setComercio] = useState(null);
  const [pedidos, setPedidos] = useState([]);
  const [depositos, setDepositos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelado = false;

    async function cargar() {
      try {
        const data = propio ? await getMiComercio() : await getComercioPorId(id);
        if (cancelado) return;
        setComercio(data);

        const [pedidosData, depositosData] = await Promise.all([
          getPedidosPorComercio(data.id).catch(() => []),
          getDepositosPorComercio(data.id).catch(() => []),
        ]);
        if (cancelado) return;
        setPedidos(pedidosData ?? []);
        setDepositos(depositosData ?? []);
      } catch (err) {
        if (!cancelado) setError(err.message);
      } finally {
        if (!cancelado) setLoading(false);
      }
    }

    cargar();

    return () => {
      cancelado = true;
    };
  }, [id, propio]);

  return { comercio, pedidos, depositos, loading, error };
}
