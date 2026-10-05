import { useEffect, useState } from "react";
import {
  getDepositos,
  getInventarioPorComercio,
  getItemsInventario,
  getItemsPorDeposito,
  getItemsPorInventario,
} from "../api/inventarioApi";
import { getComercios, getMiComercio } from "../api/comerciosApi";
import { getDepositosPorComercio, getMiDeposito } from "../api/depositosApi";
import { getPedidos, getPedidosPorComercio } from "../api/pedidosApi";
import { getProductos } from "../api/productosApi";
import { getRepartidores } from "../api/repartidoresApi";
import { getMiHistorialRepartidor, getMiPerfilRepartidor } from "../api/repartidorPerfilApi";
import { getUsuariosAdmin } from "../api/usuariosApi";
import { construirAlertas } from "../src/dashboard/utils/alertas";
import { contarPorEstado } from "../src/dashboard/utils/pedidoEstado";

/**
 * Carga desde el backend todo lo que necesita el Dashboard según el rol.
 *
 * Devuelve:
 *  - metrics: números/textos para las tarjetas (ver dashboardConfig.js)
 *  - data:    listas para los paneles (pedidos, repartidores, alertas, etc.)
 *  - errores: nombres de las fuentes que no se pudieron cargar. Una fuente
 *             caída no rompe el resto del dashboard.
 */
export function useDashboardMetrics(user) {
  const [state, setState] = useState({ metrics: {}, data: {}, errores: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;

    let cancelado = false;

    async function cargar() {
      setLoading(true);
      const errores = [];

      // Resuelve una fuente; si falla, la anota y devuelve el valor por defecto.
      const fuente = (nombre, promesa, porDefecto) =>
        promesa.catch(() => {
          errores.push(nombre);
          return porDefecto;
        });

      let resultado = { metrics: {}, data: {} };

      if (user.rol === "ADMIN") {
        resultado = await cargarAdmin(fuente);
      } else if (user.rol === "COMERCIO") {
        resultado = await cargarComercio(fuente);
      } else if (user.rol === "DEPOSITO") {
        resultado = await cargarDeposito(fuente);
      } else if (user.rol === "REPARTIDOR") {
        resultado = await cargarRepartidor(fuente);
      }

      if (!cancelado) {
        setState({ ...resultado, errores });
        setLoading(false);
      }
    }

    cargar();

    return () => {
      cancelado = true;
    };
  }, [user?.rol, user?.id]); // eslint-disable-line react-hooks/exhaustive-deps

  return { ...state, loading };
}

// ─────────────────────────────── ADMIN ───────────────────────────────

async function cargarAdmin(fuente) {
  const [comercios, depositos, usuarios, pedidos, repartidores, items] = await Promise.all([
    fuente("comercios", getComercios(), []),
    fuente("depósitos", getDepositos(), []),
    fuente("usuarios", getUsuariosAdmin(), []),
    fuente("pedidos", getPedidos(), []),
    fuente("repartidores", getRepartidores(), []),
    fuente("inventario", getItemsInventario(), []),
  ]);

  const nombrePorComercio = Object.fromEntries(
    comercios.map((c) => [c.id, c.nombreComercial])
  );
  const pedidosConComercio = pedidos.map((p) => ({
    ...p,
    comercioNombre: nombrePorComercio[p.comercioId] ?? `Comercio Nº ${p.comercioId}`,
  }));

  const usuariosPendientes = usuarios.filter((u) => u.estado === "EN_EVALUACION").length;

  return {
    metrics: {
      totalPedidos: pedidos.length,
      pedidosPorEstado: contarPorEstado(pedidos),
      totalComercios: comercios.length,
      comerciosActivos: comercios.filter((c) => c.estado === "VALIDADO").length,
      totalDepositos: depositos.length,
      totalRepartidores: repartidores.length,
      repartidoresDisponibles: repartidores.filter(
        (r) => r.activo && r.estado === "DISPONIBLE"
      ).length,
      repartidoresEnEntrega: repartidores.filter((r) => r.estado === "EN_ENTREGA").length,
      usuariosPendientes,
    },
    data: {
      pedidos: pedidosConComercio,
      repartidores,
      alertas: construirAlertas({
        items,
        pedidos,
        usuariosPendientes,
        mostrarComercio: true,
      }),
    },
  };
}

// ────────────────────────────── COMERCIO ─────────────────────────────

async function cargarComercio(fuente) {
  const comercio = await fuente("comercio", getMiComercio(), null);

  if (!comercio) {
    return { metrics: {}, data: {} };
  }

  const [pedidos, depositos, productos, inventario] = await Promise.all([
    fuente("pedidos", getPedidosPorComercio(comercio.id), []),
    fuente("depósitos", getDepositosPorComercio(comercio.id), []),
    fuente("productos", getProductos({ comercioId: comercio.id }), []),
    fuente("inventario", getInventarioPorComercio(comercio.id), null),
  ]);

  const items = inventario
    ? await fuente("ítems de inventario", getItemsPorInventario(inventario.id), [])
    : [];

  return {
    metrics: {
      totalPedidos: pedidos.length,
      pedidosPorEstado: contarPorEstado(pedidos),
      totalItemsInventario: items.length,
      stockTotal: items.reduce((acc, it) => acc + (it.cantidad ?? 0), 0),
      totalDepositos: depositos.length,
      totalProductos: productos.length,
      productosActivos: productos.filter((p) => p.estado === "ACTIVO").length,
    },
    data: {
      comercio,
      pedidos: pedidos.map((p) => ({ ...p, comercioNombre: comercio.nombreComercial })),
      alertas: construirAlertas({ items, pedidos }),
    },
  };
}

// ────────────────────────────── DEPOSITO ─────────────────────────────

async function cargarDeposito(fuente) {
  // 404 = la cuenta todavía no tiene un depósito asociado.
  const deposito = await getMiDeposito().catch(() => null);

  if (!deposito) {
    return { metrics: {}, data: { sinDeposito: true } };
  }

  const [items, pedidos] = await Promise.all([
    fuente("inventario del depósito", getItemsPorDeposito(deposito.id), []),
    deposito.comercioId
      ? fuente("pedidos", getPedidosPorComercio(deposito.comercioId), [])
      : Promise.resolve([]),
  ]);

  const porDespachar = pedidos.filter(
    (p) => p.estado === "CREADO" || p.estado === "ASIGNADO"
  ).length;

  return {
    metrics: {
      depositoNombre: deposito.nombre,
      comercioNombre: deposito.comercioNombre ?? "Sin comercio",
      totalItemsInventario: items.length,
      stockTotal: items.reduce((acc, it) => acc + (it.cantidad ?? 0), 0),
      pedidosPorDespachar: porDespachar,
      totalPedidos: pedidos.length,
      pedidosPorEstado: contarPorEstado(pedidos),
    },
    data: {
      deposito,
      pedidos: pedidos.map((p) => ({ ...p, comercioNombre: deposito.comercioNombre })),
      alertas: construirAlertas({ items, pedidos }),
    },
  };
}

// ───────────────────────────── REPARTIDOR ────────────────────────────

async function cargarRepartidor(fuente) {
  // 404 = el admin todavía no creó el perfil de repartidor para este usuario.
  const perfil = await getMiPerfilRepartidor().catch(() => null);

  if (!perfil) {
    return { metrics: {}, data: { sinPerfil: true } };
  }

  const historial = await fuente("historial de entregas", getMiHistorialRepartidor(), []);

  return {
    metrics: {
      estadoRepartidor: perfil.estado,
      pedidoActual: perfil.pedidoActual ?? null,
      entregados: historial.filter((h) => h.resultado === "ENTREGADO").length,
      cancelados: historial.filter((h) => h.resultado === "CANCELADO").length,
    },
    data: { perfil, historial },
  };
}
