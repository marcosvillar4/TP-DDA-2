import { useCallback, useEffect, useState } from "react";
import {
  getNotificaciones,
  marcarNotificacionLeida,
} from "../api/notificacionesApi";

export function useNotificaciones() {
  const [notificaciones, setNotificaciones] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const cargar = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setNotificaciones(await getNotificaciones());
    } catch (err) {
      setError(err.message || "No se pudieron cargar las notificaciones.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    cargar(); // eslint-disable-line react-hooks/set-state-in-effect
  }, [cargar]);

  const marcarLeida = useCallback(async (notificacion) => {
    if (notificacion.leida) return;

    try {
      const actualizada = await marcarNotificacionLeida(notificacion.id);
      setNotificaciones((actuales) =>
        actuales.map((item) =>
          item.id === actualizada.id ? actualizada : item
        )
      );
      window.dispatchEvent(new Event("notificaciones:actualizadas"));
    } catch (err) {
      setError(err.message || "No se pudo marcar la notificación como leída.");
    }
  }, []);

  return { notificaciones, loading, error, recargar: cargar, marcarLeida };
}
