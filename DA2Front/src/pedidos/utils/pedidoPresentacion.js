export function formatearDeposito(deposito, depositoId) {
  if (!deposito) return depositoId ? `Depósito #${depositoId} (datos no disponibles)` : 'No especificado';
  const nombre = deposito.nombre?.trim() || 'Nombre no disponible';
  const direccion = deposito.direccion?.trim() || 'Dirección no disponible';
  return `${nombre} (${direccion})`;
}

// Refleja los permisos del backend; no reemplaza su autorizacion por recurso.
export function accionesPedido(rol, estado) {
  const admin = rol === 'ADMIN';
  return {
    preparar: ['ADMIN', 'COMERCIO', 'DEPOSITO'].includes(rol) && estado === 'CREADO',
    asignar: admin && estado === 'LISTO_PARA_RETIRAR',
    retirar: admin && estado === 'ASIGNADO',
    viajar: admin && estado === 'RETIRADO',
    entregar: admin && estado === 'EN_CAMINO',
    cancelar: ['ADMIN', 'COMERCIO'].includes(rol)
      && ['CREADO', 'LISTO_PARA_RETIRAR', 'ASIGNADO', 'RETIRADO'].includes(estado),
  };
}
