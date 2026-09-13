export function UsuarioEditarForm({ form, guardando, handleChange, guardar, cancelar }) {
  return (
    <form className="detalle-form" onSubmit={guardar}>
      <label>Nombre
        <input name="nombre" value={form.nombre} onChange={handleChange} required />
      </label>
      <label>Apellido
        <input name="apellido" value={form.apellido} onChange={handleChange} required />
      </label>
      <label>Email
        <input name="email" type="email" value={form.email} onChange={handleChange} required />
      </label>
      <label>Teléfono
        <input name="telefono" value={form.telefono} onChange={handleChange} required />
      </label>
      <label>DNI
        <input name="dni" value={form.dni} onChange={handleChange} required />
      </label>
      <div className="detalle-form-botones">
        <button type="submit" className="btn-guardar" disabled={guardando}>
          {guardando ? "Guardando..." : "Guardar cambios"}
        </button>
        <button type="button" className="btn-cancelar" onClick={cancelar}>Cancelar</button>
      </div>
    </form>
  );
}