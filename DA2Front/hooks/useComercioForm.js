import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Swal from "sweetalert2";
import { actualizarComercio, getComercioPorId } from "../api/comerciosApi";

const CONFIRM_COLOR = "#16223f";

const FORM_INICIAL = {
  nombreComercial: "",
  razonSocial: "",
  cuit: "",
  email: "",
  telefono: "",
  direccion: "",
};

/**
 * Edición de un comercio existente (PUT /comercios/{id}).
 * Los comercios se crean por registro (POST /auth/register con rol COMERCIO),
 * por eso este hook no tiene modo "alta".
 *
 * @param {string} id        - id del comercio
 * @param {string} volverA   - ruta a la que se vuelve al guardar/cancelar
 */
export function useComercioForm(id, volverA) {
  const navigate = useNavigate();
  const [form, setForm] = useState(FORM_INICIAL);
  const [estado, setEstado] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelado = false;

    getComercioPorId(id)
      .then((c) => {
        if (cancelado) return;
        setForm({
          nombreComercial: c.nombreComercial ?? "",
          razonSocial: c.razonSocial ?? "",
          cuit: c.cuit ?? "",
          email: c.email ?? "",
          telefono: c.telefono ?? "",
          direccion: c.direccion ?? "",
        });
        setEstado(c.estado ?? null);
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
  }, [id]);

  function handleChange(e) {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);

    try {
      await actualizarComercio(id, form);

      await Swal.fire({
        icon: "success",
        title: "Comercio actualizado",
        timer: 1300,
        showConfirmButton: false,
      });

      navigate(volverA);
    } catch (err) {
      Swal.fire({
        icon: "error",
        title: "No se pudo guardar el comercio",
        text: err.message,
        confirmButtonColor: CONFIRM_COLOR,
      });
    } finally {
      setSaving(false);
    }
  }

  return { form, estado, loading, saving, error, handleChange, handleSubmit };
}
