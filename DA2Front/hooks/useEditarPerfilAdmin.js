import { useEffect, useState } from "react";
import Swal from "sweetalert2";
import { actualizarPerfilAdmin } from "../api/usuariosApi";
 
// Mismos formatos que el @Pattern del backend: AAA999 (viejo) o AA999AA (Mercosur).
const PATENTE_REGEX = /^([A-Z]{3}\d{3}|[A-Z]{2}\d{3}[A-Z]{2})$/;
 
const toast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 2200,
  timerProgressBar: true,
});
 
/**
 * Edición de los datos propios del rol (comercio / repartidor / depósito).
 * Es agnóstico del rol: quien lo usa le pasa los valores iniciales del formulario
 * y el backend decide a qué módulo escribir según el rol real del usuario.
 */
export function useEditarPerfilAdmin(usuario, onActualizado) {
  const [editando, setEditando] = useState(false);
  const [form, setForm] = useState(null);
  const [guardando, setGuardando] = useState(false);
 
  // Si cambia el usuario mostrado (cola de revisión, botones del navegador) se descarta
  // la edición: evita guardar datos de un usuario sobre el id de otro.
  useEffect(() => {
    setEditando(false);
    setForm(null);
  }, [usuario?.id]);
 
  function iniciarEdicion(valoresIniciales) {
    setForm(valoresIniciales);
    setEditando(true);
  }
 
  function cancelar() {
    setEditando(false);
    setForm(null);
  }
 
  function handleChange(e) {
    const { name, value } = e.target;
    const finalValue = name === "patente" ? value.toUpperCase().replace(/\s/g, "") : value;
    setForm((prev) => ({ ...prev, [name]: finalValue }));
  }
 
  async function guardar(e) {
    e.preventDefault();
 
    if (usuario.rol === "REPARTIDOR" && !PATENTE_REGEX.test(form.patente ?? "")) {
      Swal.fire({
        icon: "warning",
        title: "Patente inválida",
        text: "El formato debe ser AAA999 o AA999AA.",
        confirmButtonColor: "#16223f",
      });
      return;
    }
 
    setGuardando(true);
    try {
      const actualizado = await actualizarPerfilAdmin(usuario.id, { rol: usuario.rol, ...form });
      onActualizado(actualizado);
      cancelar();
      toast.fire({ icon: "success", title: "Datos actualizados" });
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudieron guardar los cambios",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
    } finally {
      setGuardando(false);
    }
  }
 
  return { editando, form, guardando, iniciarEdicion, cancelar, handleChange, guardar };
}