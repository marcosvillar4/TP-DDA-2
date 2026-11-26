import { useEffect, useState } from "react";
import { Sun, Moon } from "lucide-react";
import Swal from "sweetalert2";
import { useAuthUser } from "../../hooks/useAuthUser";
import { actualizarMiTema, getMiUsuario } from "../../api/usuariosApi";
import { aplicarTema, guardarTemaEnSesion, TEMAS } from "../theme/tema";
import "./styles/ConfiguracionPage.css";

const OPCIONES_TEMA = [
  {
    valor: TEMAS.CLARO,
    titulo: "Claro",
    descripcion: "Fondo blanco, ideal para ambientes iluminados.",
    Icono: Sun,
  },
  {
    valor: TEMAS.OSCURO,
    titulo: "Oscuro",
    descripcion: "Fondo oscuro, más cómodo con poca luz.",
    Icono: Moon,
  },
];

function formatearFecha(iso) {
  if (!iso) return "—";
  const fecha = new Date(iso);
  if (Number.isNaN(fecha.getTime())) return "—";
  return fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "long", year: "numeric" });
}

export default function ConfiguracionPage() {
  const user = useAuthUser();
  const [tema, setTema] = useState(user?.tema ?? TEMAS.CLARO);
  const [guardando, setGuardando] = useState(false);
  const [cuenta, setCuenta] = useState(null);

  useEffect(() => {
    let activo = true;
    getMiUsuario()
      .then((datos) => {
        if (activo) setCuenta(datos);
      })
      .catch(() => {
        // Los datos de cuenta son informativos: si falla, se muestran guiones.
      });
    return () => {
      activo = false;
    };
  }, []);

  async function elegirTema(nuevoTema) {
    if (nuevoTema === tema || guardando) return;

    const anterior = tema;
    setTema(nuevoTema);
    aplicarTema(nuevoTema);
    setGuardando(true);

    try {
      const actualizado = await actualizarMiTema(nuevoTema);
      guardarTemaEnSesion(actualizado.tema);
    } catch (err) {
      setTema(anterior);
      aplicarTema(anterior);
      Swal.fire({
        icon: "error",
        title: "No se pudo guardar el tema",
        text: err.message,
        confirmButtonColor: "#16223f",
      });
    } finally {
      setGuardando(false);
    }
  }

  const nombreCompleto = [user?.nombre, user?.apellido].filter(Boolean).join(" ") || "—";

  return (
    <div className="config-page">
      <section className="config-card">
        <h2 className="config-card-title">Apariencia</h2>
        <p className="config-card-text">
          Elegí cómo querés ver LogiRed. Se guarda en tu cuenta y se aplica en todos tus dispositivos.
        </p>

        <div className="config-temas" role="radiogroup" aria-label="Tema de la interfaz">
          {OPCIONES_TEMA.map(({ valor, titulo, descripcion, Icono }) => (
            <button
              key={valor}
              type="button"
              role="radio"
              aria-checked={tema === valor}
              className={`config-tema ${tema === valor ? "config-tema-activo" : ""}`}
              onClick={() => elegirTema(valor)}
              disabled={guardando}
            >
              <Icono size={22} aria-hidden="true" />
              <span className="config-tema-titulo">{titulo}</span>
              <span className="config-tema-desc">{descripcion}</span>
            </button>
          ))}
        </div>
      </section>

      <section className="config-card">
        <h2 className="config-card-title">Mi cuenta</h2>
        <dl className="config-datos">
          <div>
            <dt>Nombre</dt>
            <dd>{nombreCompleto}</dd>
          </div>
          <div>
            <dt>Email</dt>
            <dd>{user?.email ?? "—"}</dd>
          </div>
          <div>
            <dt>Miembro desde</dt>
            <dd>{formatearFecha(cuenta?.fechaCreacion)}</dd>
          </div>
        </dl>
      </section>
    </div>
  );
}