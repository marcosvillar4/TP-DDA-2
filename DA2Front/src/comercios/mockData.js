export const comerciosMock = [
  {
    id: 'COM-001',
    nombre: 'Urban Shoes',
    responsable: 'Martín Álvarez',
    cuit: '30-71234567-8',
    email: 'info@urbanshoes.com.ar',
    telefono: '+54 11 4234-5678',
    direccion: 'Av. Corrientes 1234, CABA',
    estado: 'Activo',
    stats: { totales: 148, mes: 42, pendientes: 18 }
  },
  {
    id: 'COM-002',
    nombre: 'TecnoStore',
    responsable: 'Valeria Suárez',
    cuit: '30-68901234-5',
    email: 'ventas@tecnostore.com.ar',
    telefono: '+54 11 4890-1234',
    direccion: 'Av. Santa Fe 4567, CABA',
    estado: 'Activo',
    stats: { totales: 320, mes: 85, pendientes: 5 }
  },
  {
    id: 'COM-003',
    nombre: 'Casa Norte',
    responsable: 'Jorge Peralta',
    cuit: '30-54321098-7',
    email: 'pedidos@casanorte.com.ar',
    telefono: '+54 11 4543-2109',
    direccion: 'Av. Cabildo 890, CABA',
    estado: 'Activo',
    stats: { totales: 95, mes: 20, pendientes: 2 }
  },
  {
    id: 'COM-004',
    nombre: 'MegaSport',
    responsable: 'Claudia Ferreyra',
    cuit: '30-45678901-2',
    email: 'logistica@megasport.com.ar',
    telefono: '+54 11 4567-8901',
    direccion: 'Av. Rivadavia 2345, CABA',
    estado: 'Inactivo',
    stats: { totales: 412, mes: 0, pendientes: 0 }
  },
  {
    id: 'COM-005',
    nombre: 'ElectroHogar',
    responsable: 'Ricardo Soto',
    cuit: '30-32109876-5',
    email: 'operaciones@electrohogar.com.ar',
    telefono: '+54 11 4321-0987',
    direccion: 'Av. del Libertador 1567, CABA',
    estado: 'Activo',
    stats: { totales: 210, mes: 55, pendientes: 8 }
  }
];

export const pedidosRecientesMock = [
  { id: 'LOG-0031', destinatario: 'María García', estado: 'En tránsito', repartidor: 'Carlos Ruiz', fecha: '24/08/2026' },
  { id: 'LOG-0028', destinatario: 'Roberto Silva', estado: 'Preparando', repartidor: 'Miguel Torres', fecha: '23/08/2026' },
  { id: 'LOG-0025', destinatario: 'Sofía Castro', estado: 'En tránsito', repartidor: 'Carlos Ruiz', fecha: '21/08/2026' },
  { id: 'LOG-0023', destinatario: 'Carolina Vega', estado: 'Pendiente', repartidor: '—', fecha: '20/08/2026' }
];
