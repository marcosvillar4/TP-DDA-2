/** Par etiqueta/valor con ícono. Si el valor viene vacío muestra "No informado". */
export function InfoItem({ icono: Icono, label, ancho = false, children }) {
  const vacio = children === null || children === undefined || children === "";
  return (
    <div className={`info-item${ancho ? " info-item-ancho" : ""}`}>
      <span className="info-item-icono"><Icono size={16} /></span>
      <div className="info-item-contenido">
        <span className="detalle-label">{label}</span>
        <div className={`info-item-valor${vacio ? " vacio" : ""}`}>
          {vacio ? "No informado" : children}
        </div>
      </div>
    </div>
  );
}