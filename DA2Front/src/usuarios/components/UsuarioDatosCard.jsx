export function UsuarioDatosCard({ usuario }) {
  return (
    <div className="detalle-datos">
      <div><span className="detalle-label">Email</span><p>{usuario.email}</p></div>
      <div><span className="detalle-label">Teléfono</span><p>{usuario.telefono}</p></div>
      <div><span className="detalle-label">DNI</span><p>{usuario.dni}</p></div>
    </div>
  );
}