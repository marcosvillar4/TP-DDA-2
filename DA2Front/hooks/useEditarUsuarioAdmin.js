import { useState } from "react";
import Swal from "sweetalert2";
import { actualizarUsuarioAdmin, resetearPasswordAdmin } from "../api/usuariosApi";

export function useEditarUsuarioAdmin(usuario, onActualizado) {
  const [editando, setEditando] = useState(false);
  const [form, setForm] = useState(null);
  const [guardando, setGuardando] = useState(false);

  function iniciarEdicion() {
    setForm({
      nombre: usuario.nombre,
      apellido: usuario.apellido,
      telefono: usuario.telefono,
      email: usuario.email,
      dni: usuario.dni || "",
    });
    setEditando(true);
  }

  function cancelar() {
    setEditando(false);
    setForm(null);
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  async function guardar(e) {
    e.preventDefault();
    setGuardando(true);
    try {
      const actualizado = await actualizarUsuarioAdmin(usuario.id, form);
      onActualizado(actualizado);
      setEditando(false);
      Swal.fire({ icon: "success", title: "Datos actualizados", confirmButtonColor: "#16223f" });
    } catch (err) {
      Swal.fire({ icon: "error", title: "Error", text: err.message, confirmButtonColor: "#16223f" });
    } finally {
      setGuardando(false);
    }
  }

  async function resetearPassword() {
    const { value: passwordNuevo } = await Swal.fire({
      title: "Resetear contraseña",
      input: "password",
      inputLabel: "Nueva contraseña (mínimo 8 caracteres)",
      inputValidator: (value) => (!value || value.length < 8 ? "Mínimo 8 caracteres" : null),
      showCancelButton: true,
      confirmButtonText: "Resetear",
      confirmButtonColor: "#16223f",
    });
    if (!passwordNuevo) return;

    try {
      await resetearPasswordAdmin(usuario.id, { passwordNuevo });
      Swal.fire({ icon: "success", title: "Contraseña reseteada", confirmButtonColor: "#16223f" });
    } catch (err) {
      Swal.fire({ icon: "error", title: "Error", text: err.message, confirmButtonColor: "#16223f" });
    }
  }

  return { editando, form, guardando, iniciarEdicion, cancelar, handleChange, guardar, resetearPassword };
}