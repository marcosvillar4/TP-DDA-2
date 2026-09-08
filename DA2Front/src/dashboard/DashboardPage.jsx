import { Package, Clock, Truck, CheckCircle2, Users, Bell } from 'lucide-react';
import KpiCard from './components/KpiCard';
import EstadoPedidos from './components/EstadoPedidos';
import AlertasOperativas from './components/AlertasOperativas';
import PedidosRecientes from './components/PedidosRecientes';
import RepartidoresPanel from './components/RepartidoresPanel';
import './DashboardPage.css';

const kpis = [
  { label: 'Pedidos Totales', value: '342', sub: '+12% esta semana', icon: Package, color: 'blue' },
  { label: 'Pendientes', value: '47', sub: '5 nuevos hoy', icon: Clock, color: 'amber' },
  { label: 'En Tránsito', value: '89', sub: '3 depósitos activos', icon: Truck, color: 'violet' },
  { label: 'Entregados Hoy', value: '168', sub: '+8% vs ayer', icon: CheckCircle2, color: 'green' },
  { label: 'Repartidores Disp.', value: '12', sub: 'de 35 en total', icon: Users, color: 'blue' },
  { label: 'Alertas Activas', value: '5', sub: '2 críticas urgentes', icon: Bell, color: 'red' },
];

export default function DashboardPage() {
  return (
    <div className="dashboard">
      <div className="dashboard-kpis">
        {kpis.map((kpi) => (
          <KpiCard key={kpi.label} {...kpi} />
        ))}
      </div>
      <div className="dashboard-mid">
        <EstadoPedidos />
        <AlertasOperativas />
      </div>
      <div className="dashboard-bottom">
        <PedidosRecientes />
        <RepartidoresPanel />
      </div>
    </div>
  );
}
