export const DASHBOARD_WIDGETS_BY_ROLE = {
  ADMIN: [
    {
      key: "resumen-pedidos",
      title: "Pedidos totales",
      status: "EN TRABAJO",
      description: "Totales, pendientes, en tránsito y entregados de todo el sistema.",
      metric: (m) => m.totalPedidos,
      metricDescription: (m) =>
        `${m.pedidosPorEstado?.CREADO ?? 0} creados · ${m.pedidosPorEstado?.EN_CAMINO ?? 0} en tránsito · ${m.pedidosPorEstado?.ENTREGADO ?? 0} entregados`,
    },
    {
      key: "resumen-comercios",
      title: "Comercios activos",
      status: "PRONTO",
      description: "Cantidad de comercios dados de alta y su estado.",
      metric: (m) => m.totalComercios,
      metricDescription: () => "Comercios registrados en el sistema.",
    },
    {
      key: "resumen-depositos",
      title: "Depósitos",
      status: "PRONTO",
      description: "Ocupación y stock crítico por depósito.",
      metric: (m) => m.totalDepositos,
      metricDescription: () => "Depósitos registrados en el sistema.",
    },
    {
      key: "resumen-repartidores",
      title: "Repartidores",
      status: "PRONTO",
      description: "Disponibilidad y carga de trabajo de la flota.",
      metric: (m) => m.totalRepartidores,
      metricDescription: () => "Repartidores registrados (todos los estados).",
    },
    {
      key: "alertas",
      title: "Alertas operativas",
      status: "EN TRABAJO",
      description: "Incidencias críticas y advertencias recientes.",
    },
  ],
  COMERCIO: [
    {
      key: "resumen-pedidos",
      title: "Mis pedidos",
      status: "EN TRABAJO",
      description: "Estado de los pedidos generados por tu comercio.",
      metric: (m) => m.totalPedidos,
      metricDescription: (m) =>
        `${m.pedidosPorEstado?.CREADO ?? 0} creados · ${m.pedidosPorEstado?.EN_CAMINO ?? 0} en tránsito · ${m.pedidosPorEstado?.ENTREGADO ?? 0} entregados`,
    },
    {
      key: "resumen-inventario",
      title: "Ítems en inventario",
      status: "PRONTO",
      description: "Stock disponible por producto y depósito.",
      metric: (m) => m.totalItemsInventario,
      metricDescription: (m) => `${m.stockTotal ?? 0} unidades en total.`,
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
      key: "resumen-inventario",
      title: "Inventario del depósito",
      status: "EN TRABAJO",
      description: "Stock y movimientos recientes del depósito.",
    },
    {
      key: "resumen-pedidos",
      title: "Pedidos por despachar",
      status: "PRONTO",
      description: "Paquetes pendientes de preparación.",
    },
  ],
  REPARTIDOR: [
    {
      key: "mis-pedidos",
      title: "Mis pedidos",
      status: "EN TRABAJO",
      description: "Entregas asignadas para hoy.",
    },
    {
      key: "mi-rendimiento",
      title: "Mi rendimiento",
      status: "PRONTO",
      description: "Entregas realizadas y tiempos promedio.",
    },
  ],
};