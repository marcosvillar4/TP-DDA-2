import { useEffect, useState } from "react";
import Swal from "sweetalert2";
import {
  fetchUsuarioPorId,
  validarUsuario,
  rechazarUsuario,
  bloquearUsuario,
  desbloquearUsuario,
} from "../api/usuariosApi";

export function useUsuarioDetail(id) {
  const [usuario, setUsuario] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [accionEnCurso, setAccionEnCurso] = useState(false);

  async function cargar() {
    setLoading(true);
    setError(null);
    try {
      setUsuario(await fetchUsuarioPorId(id));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { cargar(); }, [id]);

  async function ejecutar(accionFn, confirmacion) {
    const resultado = await Swal.fire({
      icon: "question",
      title: confirmacion.titulo,
      text: confirmacion.texto,
      showCancelButton: true,
      confirmButtonText: confirmacion.confirmar,
      cancelButtonText: "Cancelar",
      confirmButtonColor: "#16223f",
    });
    if (!resultado.isConfirmed) return;

    setAccionEnCurso(true);
    try {
      setUsuario(await accionFn(id));
      Swal.fire({ icon: "success", title: "Listo", confirmButtonColor: "#16223f" });
    } catch (err) {
      Swal.fire({ icon: "error", title: "No se pudo completar", text: err.message, confirmButtonColor: "#16223f" });
    } finally {
      setAccionEnCurso(false);
    }
  }

  return {
    usuario,
    setUsuario, // se expone para que useEditarUsuarioAdmin pueda actualizarlo tras guardar
    loading,
    error,
    accionEnCurso,
    validar: () => ejecutar(validarUsuario, { titulo: "¿Validar usuario?", texto: "Podrá iniciar sesión una vez validado.", confirmar: "Sí, validar" }),
    rechazar: () => ejecutar(rechazarUsuario, { titulo: "¿Rechazar usuario?", texto: "No podrá volver a intentar validarse.", confirmar: "Sí, rechazar" }),
    bloquear: () => ejecutar(bloquearUsuario, { titulo: "¿Bloquear usuario?", texto: "No podrá iniciar sesión hasta ser desbloqueado.", confirmar: "Sí, bloquear" }),
    desbloquear: () => ejecutar(desbloquearUsuario, { titulo: "¿Desbloquear usuario?", texto: "Podrá volver a iniciar sesión.", confirmar: "Sí, desbloquear" }),
  };
}