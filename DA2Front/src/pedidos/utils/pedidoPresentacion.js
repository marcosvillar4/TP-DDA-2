export function formatearDeposito(deposito, depositoId) {
  if (!deposito) return depositoId ? `Depósito #${depositoId} (datos no disponibles)` : 'No especificado';
  const nombre = deposito.nombre?.trim() || 'Nombre no disponible';
  const direccion = deposito.direccion?.trim() || 'Dirección no disponible';
  return `${nombre} (${direccion})`;
}

// Refleja los permisos del backend; no reemplaza su autorizacion por recurso.
export function accionesPedido(rol, estado, tipoEnvio = 'LOGIRED') {
  const admin = rol === 'ADMIN';
  const externo = tipoEnvio === 'EXTERNO';
  return {
    preparar: !externo && ['ADMIN', 'COMERCIO', 'DEPOSITO'].includes(rol) && estado === 'CREADO',
    asignar: !externo && admin && estado === 'LISTO_PARA_RETIRAR',
    retirar: !externo && admin && estado === 'ASIGNADO',
    viajar: !externo && admin && estado === 'RETIRADO',
    entregar: !externo && admin && estado === 'EN_CAMINO',
    conTransportista: externo && ['ADMIN', 'COMERCIO'].includes(rol) && estado === 'CREADO',
    enviado: externo && ['ADMIN', 'COMERCIO'].includes(rol) && estado === 'CON_TRANSPORTISTA',
    cancelar: ['ADMIN', 'COMERCIO'].includes(rol)
      && ['CREADO', 'LISTO_PARA_RETIRAR', 'ASIGNADO', 'RETIRADO', 'CON_TRANSPORTISTA'].includes(estado),
  };
}
