import { VEHICULO_LABELS } from "../../utils/usuarioLabels";

export function CamposComercio({ form, onChange }) {
  return (
    <>
      <label>Nombre comercial
        <input name="nombreComercial" value={form.nombreComercial} onChange={onChange} required />
      </label>
      <label>Razón social
        <input name="razonSocial" value={form.razonSocial} onChange={onChange} required />
      </label>
      <label>CUIT
        <input name="cuit" value={form.cuit} onChange={onChange} required />
      </label>
      <label>Teléfono comercial
        <input name="telefono" type="tel" value={form.telefono} onChange={onChange} required />
      </label>
      <label className="campo-ancho">Email comercial
        <input name="email" type="email" value={form.email} onChange={onChange} required />
      </label>
      <label className="campo-ancho">Dirección de retiro
        <input name="direccion" value={form.direccion} onChange={onChange} required />
      </label>
    </>
  );
}

export function CamposRepartidor({ form, onChange }) {
  return (
    <>
      <label>Tipo de vehículo
        <select name="vehiculo" value={form.vehiculo} onChange={onChange} required>
          <option value="" disabled>Seleccione...</option>
          {Object.entries(VEHICULO_LABELS).map(([valor, label]) => (
            <option key={valor} value={valor}>{label}</option>
          ))}
        </select>
      </label>
      <label>Patente
        <input name="patente" value={form.patente} onChange={onChange} maxLength={7} required />
      </label>
      <label className="campo-ancho">Zona de reparto
        <input name="zona" value={form.zona} onChange={onChange} required />
      </label>
    </>
  );
}

export function CamposDeposito({ form, onChange }) {
  return (
    <>
      <label className="campo-ancho">Nombre del depósito
        <input name="nombre" value={form.nombre} onChange={onChange} required />
      </label>
      <label className="campo-ancho">Dirección
        <input name="direccion" value={form.direccion} onChange={onChange} required />
      </label>
    </>
  );
}