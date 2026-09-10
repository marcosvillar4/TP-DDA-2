import { Navigate, Outlet } from 'react-router-dom';

export default function ProtectedRoute({ allowedRoles }) {
  let userRole = null;
  try {
    const raw = localStorage.getItem('logired_user');
    if (raw) {
      userRole = JSON.parse(raw).rol;
    }
  } catch {}

  // If no user is logged in, they shouldn't even be in AppShell, but just in case:
  if (!userRole) {
    return <Navigate to="/" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(userRole)) {
    // If they are logged in but don't have access to this route, kick them to dashboard
    return <Navigate to="/dashboard" replace />;
  }

  // If allowed, render the nested routes (Outlet)
  return <Outlet />;
}
