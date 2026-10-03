import { useCallback, useEffect, useState } from "react";
import Swal from "sweetalert2";
import { getMiComercio } from "../api/comerciosApi";
import { agregarDeposito, getDepositosPorComercio } from "../api/depositosApi";

const CONFIRM_COLOR = "#16223f";

/**
 * Depósitos del comercio del usuario autenticado (rol COMERCIO):
 * listado y alta. Cada depósito nuevo queda vinculado a ese comercio.
 */
export function useDepositos() {
  const [comercio, setComercio] = useState(null);
  const [depositos, setDepositos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  const cargar = useCallback(async () => {
    try {
      const miComercio = await getMiComercio();
      setComercio(miComercio);

      const data = await getDepositosPorComercio(miComercio.id);
      setDepositos(data ?? []);
      setError("");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    cargar();
  }, [cargar]);

  /** @returns {Promise<boolean>} true si el depósito se creó correctamente */
  async function handleAgregarDeposito(datos) {
    if (datos.password !== datos.confirmPassword) {
      Swal.fire({
        icon: "warning",
        title: "Contraseñas distintas",
        text: "Las contraseñas no coinciden. Verificalas e intentá de nuevo.",
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    }

    setSubmitting(true);
    try {
      await agregarDeposito({
        email: datos.email,
        password: datos.password,
        nombre: datos.nombre,
        apellido: datos.apellido,
        dni: datos.dni,
        telefono: datos.telefono,
        nombreDeposito: datos.nombreDeposito,
        direccionDeposito: datos.direccionDeposito,
      });

      await Swal.fire({
        icon: "success",
        title: "Depósito agregado",
        text: "Quedó vinculado a tu comercio. La cuenta del responsable debe ser validada por un administrador antes de poder ingresar.",
        confirmButtonColor: CONFIRM_COLOR,
      });

      await cargar();
      return true;
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo agregar el depósito",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
      return false;
    } finally {
      setSubmitting(false);
    }
  }

  return { comercio, depositos, loading, submitting, error, handleAgregarDeposito };
}
