import { Building2, Navigation, Warehouse, Pencil } from "lucide-react";
import { Panel } from "./Panel";
import { PerfilComercio } from "./perfil/PerfilComercio";
import { PerfilRepartidor } from "./perfil/PerfilRepartidor";
import { PerfilDeposito } from "./perfil/PerfilDeposito";
import { ComercioDepositosPanel } from "./perfil/ComercioDepositosPanel";
import { CamposComercio, CamposRepartidor, CamposDeposito } from "./perfil/PerfilCampos";

/**
 * Registro rol -> sección. Para sumar un rol: su vista, sus campos de edición y una
 * entrada acá. "aForm" convierte el perfil que manda el backend en valores de formulario
 * (siempre strings, para que los inputs sean controlados). "Complemento" es un panel
 * opcional de solo lectura que se muestra a continuación (p. ej. depósitos de un comercio).
 */
const PERFIL_POR_ROL = {
  COMERCIO: {
    titulo: "Datos del comercio",
    icono: Building2,
    vacio: "Este usuario no tiene un comercio asociado.",
    Vista: PerfilComercio,
    Campos: CamposComercio,
    Complemento: ComercioDepositosPanel,
    aForm: (p) => ({
      nombreComercial: p.nombreComercial ?? "",
      razonSocial: p.razonSocial ?? "",
      cuit: p.cuit ?? "",
      telefono: p.telefono ?? "",
      email: p.email ?? "",
      direccion: p.direccion ?? "",
    }),
  },
  REPARTIDOR: {
    titulo: "Datos del repartidor",
    icono: Navigation,
    vacio: "Este usuario todavía no tiene perfil de repartidor.",
    Vista: PerfilRepartidor,
    Campos: CamposRepartidor,
    aForm: (p) => ({
      vehiculo: p.vehiculo ?? "",
      patente: p.patente ?? "",
      zona: p.zona ?? "",
    }),
  },
  DEPOSITO: {
    titulo: "Datos del depósito",
    icono: Warehouse,
    vacio: "Este usuario no tiene un depósito asociado.",
    Vista: PerfilDeposito,
    Campos: CamposDeposito,
    aForm: (p) => ({
      nombre: p.nombre ?? "",
      direccion: p.direccion ?? "",
    }),
  },
};

export function UsuarioPerfilSection({ usuario, edicion }) {
  const config = PERFIL_POR_ROL[usuario.rol];
  if (!config) return null; // ADMIN: no tiene datos propios

  const { titulo, icono, vacio, Vista, Campos, Complemento, aForm } = config;
  const perfil = usuario.perfil;

  return (
    <>
      <Panel
        titulo={titulo}
        icono={icono}
        acciones={
          perfil &&
          !edicion.editando && (
            <button
              type="button"
              className="u-btn u-btn-ghost"
              onClick={() => edicion.iniciarEdicion(aForm(perfil))}
            >
              <Pencil size={14} /> Editar
            </button>
          )
        }
      >
        {!perfil ? (
          <p className="detalle-panel-vacio">{vacio}</p>
        ) : edicion.editando && edicion.form ? (
          <form className="detalle-form" onSubmit={edicion.guardar}>
            <div className="detalle-form-grid">
              <Campos form={edicion.form} onChange={edicion.handleChange} />
            </div>
            <div className="detalle-form-botones">
              <button type="button" className="u-btn u-btn-ghost" onClick={edicion.cancelar} disabled={edicion.guardando}>
                Cancelar
              </button>
              <button type="submit" className="u-btn u-btn-primary" disabled={edicion.guardando}>
                {edicion.guardando ? "Guardando..." : "Guardar cambios"}
              </button>
            </div>
          </form>
        ) : (
          <Vista perfil={perfil} />
        )}
      </Panel>

      {Complemento && perfil && <Complemento perfil={perfil} />}
    </>
  );
}