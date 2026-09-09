import "../styles/EnTrabajoPage.css";

export function EnTrabajoPage({ titulo }) {
  return (
    <div className="en-trabajo-page">
      <span className="en-trabajo-badge">EN TRABAJO</span>
      <h2 className="en-trabajo-title">{titulo}</h2>
      <p className="en-trabajo-text">
        Esta sección todavía se está construyendo. Pronto vas a poder
        gestionar {titulo.toLowerCase()} desde acá.
      </p>
    </div>
  );
}

export default EnTrabajoPage;