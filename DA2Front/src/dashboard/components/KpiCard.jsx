import '../styles/KpiCard.css';

export default function KpiCard({ label, value, sub, icon: Icon, color }) {
  return (
    <div className={`kpi-card kpi-${color}`}>
      <div className="kpi-card-top">
        <span className="kpi-label">{label}</span>
        <div className={`kpi-icon-wrapper kpi-icon-${color}`}>
          <Icon size={17} />
        </div>
      </div>
      <div className="kpi-value">{value}</div>
      <div className="kpi-sub">{sub}</div>
    </div>
  );
}
