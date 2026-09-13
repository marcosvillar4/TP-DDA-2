export function UsuariosStats({ stats }) {
  return (
    <div className="usuarios-stats">
      <div className="stat-card">
        <span className="stat-numero">{stats.total}</span>
        <span className="stat-label">Total de usuarios</span>
      </div>
      <div className="stat-card">
        <span className="stat-numero stat-verde">{stats.validados}</span>
        <span className="stat-label">Validados</span>
      </div>
      <div className="stat-card">
        <span className="stat-numero stat-ambar">{stats.enEvaluacion}</span>
        <span className="stat-label">En evaluación</span>
      </div>
      <div className="stat-card">
        <span className="stat-numero stat-rojo">{stats.bloqueados}</span>
        <span className="stat-label">Bloqueados</span>
      </div>
    </div>
  );
}