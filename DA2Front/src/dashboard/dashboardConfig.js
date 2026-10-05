import { ESTADOS_PEDIDO } from "./utils/pedidoEstado";

/**
 * Tarjetas superiores del Dashboard por rol.
 *
 * - `metric(m)`: valor a mostrar, calculado con `metrics` de useDashboardMetrics.
 *   Si se omite, la tarjeta queda como "PRONTO"/"EN TRABAJO" porque todavía
 *   no existe un endpoint que la alimente.
 * - `metricDescription(m)`: texto de apoyo bajo el valor.
 */

const plural = (n, singular, pluralForm) => (n === 1 ? singular : pluralForm);

const resumenEstados = (m) =>
  `${m.pedidosPorEstado?.CREADO ?? 0} creados · ${m.pedidosPorEstado?.EN_CAMINO ?? 0} en camino · ${m.pedidosPorEstado?.ENTREGADO ?? 0} entregados`;

export const DASHBOARD_WIDGETS_BY_ROLE = {
  ADMIN: [
    {
      key: "resumen-pedidos",
      title: "Pedidos totales",
      description: "Totales y estado de los pedidos de todo el sistema.",
      metric: (m) => m.totalPedidos,
      metricDescription: resumenEstados,
    },
    {
      key: "resumen-comercios",
      title: "Comercios",
      description: "Comercios dados de alta y su estado.",
      metric: (m) => m.totalComercios,
      metricDescription: (m) =>
        `${m.comerciosActivos ?? 0} ${plural(m.comerciosActivos, "activo", "activos")} (cuenta validada)`,
    },
    {
      key: "resumen-depositos",
      title: "Depósitos",
      description: "Depósitos registrados en el sistema.",
      metric: (m) => m.totalDepositos,
      metricDescription: () => "Depósitos vinculados a comercios.",
    },
    {
      key: "resumen-repartidores",
      title: "Repartidores",
      description: "Disponibilidad de la flota.",
      metric: (m) => m.totalRepartidores,
      metricDescription: (m) =>
        `${m.repartidoresDisponibles ?? 0} ${plural(m.repartidoresDisponibles, "disponible", "disponibles")} · ${m.repartidoresEnEntrega ?? 0} en entrega`,
    },
    {
      key: "usuarios-pendientes",
      title: "Cuentas por validar",
      description: "Usuarios en evaluación.",
      metric: (m) => m.usuariosPendientes,
      metricDescription: () => "Esperando validación de un administrador.",
    },
  ],
  COMERCIO: [
    {
      key: "resumen-pedidos",
      title: "Mis pedidos",
      description: "Estado de los pedidos generados por tu comercio.",
      metric: (m) => m.totalPedidos,
      metricDescription: resumenEstados,
    },
    {
      key: "resumen-inventario",
      title: "Ítems en inventario",
      description: "Stock disponible por producto y depósito.",
      metric: (m) => m.totalItemsInventario,
      metricDescription: (m) => `${m.stockTotal ?? 0} unidades en total.`,
    },
    {
      key: "resumen-productos",
      title: "Productos",
      description: "Productos de tu catálogo.",
      metric: (m) => m.totalProductos,
      metricDescription: (m) =>
        `${m.productosActivos ?? 0} ${plural(m.productosActivos, "activo", "activos")}`,
    },
    {
      key: "resumen-depositos",
      title: "Mis depósitos",
      description: "Depósitos vinculados a tu comercio.",
      metric: (m) => m.totalDepositos,
      metricDescription: () => "Podés agregar más desde Mis Depósitos.",
    },
    {
      key: "resumen-pagos",
      title: "Pagos y cobranzas",
      status: "PRONTO",
      description: "Cobros pendientes y liquidaciones.",
    },
  ],
  DEPOSITO: [
    {
      key: "deposito-asignado",
      title: "Mi depósito",
      description: "Todavía no tenés un depósito asociado a tu cuenta.",
      metric: (m) => m.depositoNombre,
      metricDescription: (m) => `Comercio: ${m.comercioNombre}`,
    },
    {
      key: "resumen-inventario",
      title: "Inventario del depósito",
      description: "Stock del depósito.",
      metric: (m) => m.totalItemsInventario,
      metricDescription: (m) => `${m.stockTotal ?? 0} unidades en total.`,
    },
    {
      key: "resumen-pedidos",
      title: "Pedidos por despachar",
      description: "Pedidos del comercio pendientes de despacho.",
      metric: (m) => m.pedidosPorDespachar,
      metricDescription: (m) =>
        `Creados o asignados · ${m.totalPedidos ?? 0} pedidos en total.`,
    },
  ],
  REPARTIDOR: [
    {
      key: "mi-estado",
      title: "Mi estado",
      description: "Todavía no tenés un perfil de repartidor configurado.",
      metric: (m) => ESTADOS_REPARTIDOR[m.estadoRepartidor],
      metricDescription: (m) =>
        m.pedidoActual
          ? `Pedido #${m.pedidoActual.id} (${ESTADOS_PEDIDO[m.pedidoActual.estado]?.label ?? m.pedidoActual.estado}) → ${m.pedidoActual.direccionDestino}`
          : "Sin pedido en curso.",
    },
    {
      key: "mis-entregas",
      title: "Entregas realizadas",
      description: "Pedidos entregados.",
      metric: (m) => m.entregados,
      metricDescription: (m) => `${m.cancelados ?? 0} cancelados.`,
    },
  ],
};

export const ESTADOS_REPARTIDOR = {
  DISPONIBLE: "Disponible",
  EN_ENTREGA: "En entrega",
  NO_DISPONIBLE: "No disponible",
};
