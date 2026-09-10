export const DASHBOARD_WIDGETS_BY_ROLE = {
  ADMIN: [
    {
      key: "resumen-pedidos",
      title: "Resumen de pedidos",
      status: "EN TRABAJO",
      description: "Totales, pendientes, en tránsito y entregados de todo el sistema.",
    },
    {
      key: "resumen-comercios",
      title: "Comercios activos",
      status: "PRONTO",
      description: "Cantidad de comercios dados de alta y su estado.",
    },
    {
      key: "resumen-depositos",
      title: "Depósitos",
      status: "PRONTO",
      description: "Ocupación y stock crítico por depósito.",
    },
    {
      key: "resumen-repartidores",
      title: "Repartidores",
      status: "PRONTO",
      description: "Disponibilidad y carga de trabajo de la flota.",
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
    },
    {
      key: "resumen-inventario",
      title: "Inventario",
      status: "PRONTO",
      description: "Stock disponible por producto y depósito.",
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