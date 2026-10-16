import { useEffect, useState } from "react";
import Swal from "sweetalert2";
import {
  fetchUsuarioPorId,
  validarUsuario,
  rechazarUsuario,
  bloquearUsuario,
  desbloquearUsuario,
} from "../api/usuariosApi";

const toast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 2200,
  timerProgressBar: true,
});

export function useUsuarioDetail(id) {
  const [usuario, setUsuario] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [accionEnCurso, setAccionEnCurso] = useState(false);
  const [recarga, setRecarga] = useState(0);

  useEffect(() => {
    // "cancelado" evita pisar el estado con la respuesta de un id anterior
    // si el usuario navega rápido entre detalles (cola de revisión).
    let cancelado = false;
    setLoading(true);
    setError(null);
    fetchUsuarioPorId(id)
      .then((data) => !cancelado && setUsuario(data))
      .catch((err) => !cancelado && setError(err.message))
      .finally(() => !cancelado && setLoading(false));
    return () => {
      cancelado = true;
    };
  }, [id, recarga]);

  const recargar = () => setRecarga((n) => n + 1);

  /** Devuelve true si la acción se confirmó y se ejecutó bien. */
  async function ejecutar(accionFn, { titulo, texto, confirmar, exito, peligro = false }) {
    const resultado = await Swal.fire({
      icon: peligro ? "warning" : "question",
      title: titulo,
      text: texto,
      showCancelButton: true,
      confirmButtonText: confirmar,
      cancelButtonText: "Cancelar",
      confirmButtonColor: peligro ? "#d6273c" : "#16223f",
      reverseButtons: true,
    });
    if (!resultado.isConfirmed) return false;

    setAccionEnCurso(true);
    try {
      const actualizado = await accionFn(id);
      // Se mezcla con el estado previo para no perder datos que la respuesta
      // de la transición no trae (por ejemplo, el "perfil" del rol).
      if (actualizado) setUsuario((prev) => ({ ...prev, ...actualizado }));
      else recargar();
      toast.fire({ icon: "success", title: exito });
      return true;
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo completar",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
      return false;
    } finally {
      setAccionEnCurso(false);
    }
  }

  return {
    usuario,
    setUsuario,
    loading,
    error,
    accionEnCurso,
    recargar,
    validar: () =>
      ejecutar(validarUsuario, {
        titulo: "¿Validar usuario?",
        texto: "Podrá iniciar sesión una vez validado.",
        confirmar: "Sí, validar",
        exito: "Usuario validado",
      }),
    rechazar: () =>
      ejecutar(rechazarUsuario, {
        titulo: "¿Rechazar usuario?",
        texto: "No podrá volver a intentar validarse.",
        confirmar: "Sí, rechazar",
        exito: "Usuario rechazado",
        peligro: true,
      }),
    bloquear: () =>
      ejecutar(bloquearUsuario, {
        titulo: "¿Bloquear usuario?",
        texto: "No podrá iniciar sesión hasta ser desbloqueado.",
        confirmar: "Sí, bloquear",
        exito: "Usuario bloqueado",
        peligro: true,
      }),
    desbloquear: () =>
      ejecutar(desbloquearUsuario, {
        titulo: "¿Desbloquear usuario?",
        texto: "Podrá volver a iniciar sesión.",
        confirmar: "Sí, desbloquear",
        exito: "Usuario desbloqueado",
      }),
  };
}