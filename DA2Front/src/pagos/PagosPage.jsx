import { useAuthUser } from '../../hooks/useAuthUser';
import ComercioPagos from './ComercioPagos';
import RepartidorPagos from './RepartidorPagos';

/** Muestra la vista de pagos que corresponde al rol (el acceso lo restringe RoleGuard). */
export default function PagosPage() {
  const usuario = useAuthUser();
  if (usuario?.rol === 'COMERCIO') return <ComercioPagos />;
  if (usuario?.rol === 'REPARTIDOR') return <RepartidorPagos />;
  return null;
}
