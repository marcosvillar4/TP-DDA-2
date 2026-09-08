export const pedidosMock = [
  {
    id: 'LOG-0031',
    comercio: { nombre: 'Urban Shoes', rubro: 'Calzado' },
    destinatario: 'María García',
    direccion: 'Av. Cabildo 2234, Belgrano, CABA',
    estado: 'En tránsito',
    repartidor: 'Carlos Ruiz',
    fecha: '24/08/2026',
    deposito: 'Dep. Caballito',
    productos: [
      { nombre: 'Zapatillas Running Air Max', cantidad: 2, pesoUnitario: '1.2 kg' },
      { nombre: 'Tenis Casual Lace-Up', cantidad: 1, pesoUnitario: '0.6 kg' }
    ],
    observaciones: 'Dejar en portería. Horario de entrega: 9:00 a 17:00 hs.',
    historial: [
      { estado: 'Pedido recibido', fecha: '22/08/2026 09:00', ubicacion: 'Dep. Caballito', status: 'completed' },
      { estado: 'En preparación', fecha: '22/08/2026 14:30', ubicacion: 'Dep. Caballito', status: 'completed' },
      { estado: 'Listo para despacho', fecha: '23/08/2026 08:15', ubicacion: 'Dep. Caballito', status: 'completed' },
      { estado: 'En tránsito', fecha: '24/08/2026 10:20', ubicacion: 'Zona Belgrano', status: 'current' },
      { estado: 'Entregado', fecha: '', ubicacion: '', status: 'pending' }
    ]
  },
  {
    id: 'LOG-0030',
    comercio: { nombre: 'TecnoStore', rubro: 'Electrónica' },
    destinatario: 'Juan Pérez',
    direccion: 'Av. Corrientes 890, CABA',
    estado: 'Entregado',
    repartidor: 'Ana López',
    fecha: '24/08/2026',
    deposito: 'Dep. Centro',
    productos: [
      { nombre: 'Auriculares Inalámbricos', cantidad: 1, pesoUnitario: '0.3 kg' }
    ],
    observaciones: 'Entregar en mano.',
    historial: [
      { estado: 'Pedido recibido', fecha: '23/08/2026 10:00', ubicacion: 'Dep. Centro', status: 'completed' },
      { estado: 'En preparación', fecha: '23/08/2026 11:30', ubicacion: 'Dep. Centro', status: 'completed' },
      { estado: 'Listo para despacho', fecha: '23/08/2026 15:15', ubicacion: 'Dep. Centro', status: 'completed' },
      { estado: 'En tránsito', fecha: '24/08/2026 09:20', ubicacion: 'Microcentro', status: 'completed' },
      { estado: 'Entregado', fecha: '24/08/2026 11:45', ubicacion: 'Av. Corrientes 890', status: 'completed' }
    ]
  },
  {
    id: 'LOG-0029',
    comercio: { nombre: 'Casa Norte', rubro: 'Hogar' },
    destinatario: 'Lucía Martínez',
    direccion: 'Av. Santa Fe 2456, Palermo, CABA',
    estado: 'Pendiente',
    repartidor: '—',
    fecha: '23/08/2026',
    deposito: 'Dep. Palermo',
    productos: [
      { nombre: 'Juego de sábanas Queen', cantidad: 1, pesoUnitario: '1.5 kg' },
      { nombre: 'Almohadas Viscoelásticas', cantidad: 2, pesoUnitario: '0.8 kg' }
    ],
    observaciones: '',
    historial: [
      { estado: 'Pedido recibido', fecha: '23/08/2026 18:00', ubicacion: 'Sistema central', status: 'completed' },
      { estado: 'Pendiente de asignación', fecha: '23/08/2026 18:05', ubicacion: 'Dep. Palermo', status: 'current' },
      { estado: 'En preparación', fecha: '', ubicacion: '', status: 'pending' },
      { estado: 'Listo para despacho', fecha: '', ubicacion: '', status: 'pending' },
      { estado: 'En tránsito', fecha: '', ubicacion: '', status: 'pending' },
      { estado: 'Entregado', fecha: '', ubicacion: '', status: 'pending' }
    ]
  },
  {
    id: 'LOG-0028',
    comercio: { nombre: 'Urban Shoes', rubro: 'Calzado' },
    destinatario: 'Roberto Silva',
    direccion: 'Av. Rivadavia 4512, Almagro, CABA',
    estado: 'Preparando',
    repartidor: 'Miguel Torres',
    fecha: '23/08/2026',
    deposito: 'Dep. Centro',
    productos: [
      { nombre: 'Botas de Cuero', cantidad: 1, pesoUnitario: '2.1 kg' }
    ],
    observaciones: 'Llamar al llegar.',
    historial: [
      { estado: 'Pedido recibido', fecha: '23/08/2026 08:30', ubicacion: 'Dep. Centro', status: 'completed' },
      { estado: 'En preparación', fecha: '23/08/2026 10:15', ubicacion: 'Dep. Centro', status: 'current' },
      { estado: 'Listo para despacho', fecha: '', ubicacion: '', status: 'pending' },
      { estado: 'En tránsito', fecha: '', ubicacion: '', status: 'pending' },
      { estado: 'Entregado', fecha: '', ubicacion: '', status: 'pending' }
    ]
  },
  {
    id: 'LOG-0027',
    comercio: { nombre: 'TecnoStore', rubro: 'Electrónica' },
    destinatario: 'Carmen Díaz',
    direccion: 'Av. Callao 567, Recoleta, CABA',
    estado: 'Cancelado',
    repartidor: '—',
    fecha: '22/08/2026',
    deposito: 'Dep. Norte',
    productos: [
      { nombre: 'Monitor 24 pulgadas', cantidad: 1, pesoUnitario: '3.5 kg' }
    ],
    observaciones: 'Cancelado por falta de stock.',
    historial: [
      { estado: 'Pedido recibido', fecha: '22/08/2026 11:00', ubicacion: 'Sistema central', status: 'completed' },
      { estado: 'Cancelado', fecha: '22/08/2026 12:30', ubicacion: 'Sistema central', status: 'current' }
    ]
  }
];
