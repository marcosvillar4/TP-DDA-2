import { Navigate } from 'react-router-dom';
import { useAuthUser } from '../../hooks/useAuthUser';
import { resolverAccesoRuta } from './routePermissions';

export default function RoleGuard({ roles, children }) {
  const usuario = useAuthUser();
  const acceso = resolverAccesoRuta(usuario, roles);
  if (!acceso.permitido) return <Navigate to={acceso.destino} replace />;
  return children;
}
