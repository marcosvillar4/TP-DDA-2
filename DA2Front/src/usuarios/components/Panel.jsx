export function Panel({ titulo, icono: Icono, acciones, children }) {
  return (
    <section className="detalle-panel">
      <header className="detalle-panel-header">
        <h3 className="detalle-panel-titulo">
          {Icono && (
            <span className="detalle-panel-icono">
              <Icono size={18} />
            </span>
          )}
          {titulo}
        </h3>
        {acciones}
      </header>
      <div className="detalle-panel-body">{children}</div>
    </section>
  );
}