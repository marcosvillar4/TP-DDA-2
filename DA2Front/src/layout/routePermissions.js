export const ROLES = {
  ADMIN: 'ADMIN', COMERCIO: 'COMERCIO', DEPOSITO: 'DEPOSITO', REPARTIDOR: 'REPARTIDOR',
};
const TODOS = Object.values(ROLES);
const OPERADORES_PEDIDOS = ['ADMIN', 'COMERCIO', 'DEPOSITO'];

// Permisos de pantalla; la pertenencia de cada recurso siempre se valida en el servidor.
export const PERMISOS_RUTA = {
  '/dashboard': TODOS,
  '/pedidos': OPERADORES_PEDIDOS,
  '/mis-pedidos': ['REPARTIDOR'],
  '/seguimiento': OPERADORES_PEDIDOS,
  '/comercios': ['ADMIN'],
  '/mi-comercio': ['COMERCIO'],
  '/productos': ['ADMIN', 'COMERCIO'],
  '/inventario': OPERADORES_PEDIDOS,
  '/depositos': ['ADMIN'],
  '/mis-depositos': ['COMERCIO'],
  '/repartidores': ['ADMIN'],
  '/transportistas': ['ADMIN'],
  '/pagos': OPERADORES_PEDIDOS,
  '/alertas': TODOS,
  '/usuarios': ['ADMIN'],
  '/configuracion': TODOS,
  '/comercios/:id/editar': ['ADMIN', 'COMERCIO'],
  '/comercios/:id': ['ADMIN'],
  '/pedidos/nuevo': ['ADMIN', 'COMERCIO'],
  '/pedidos/:id': OPERADORES_PEDIDOS,
  '/repartidores/:id': ['ADMIN'],
  '/depositos/:id': ['ADMIN'],
  '/seguimiento/:id': OPERADORES_PEDIDOS,
  '/usuarios/:id': ['ADMIN'],
};

export function resolverAccesoRuta(usuario, roles) {
  if (!usuario || !TODOS.includes(usuario.rol)) return { permitido: false, destino: '/' };
  if (!Array.isArray(roles) || !roles.includes(usuario.rol)) {
    return { permitido: false, destino: '/dashboard' };
  }
  return { permitido: true, destino: null };
}
