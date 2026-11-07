import test from 'node:test';
import assert from 'node:assert/strict';
import { formatearDeposito, accionesPedido } from '../src/pedidos/utils/pedidoPresentacion.js';
import { resumenRuta } from '../src/repartidor/utils/resumenRuta.js';

test('el deposito utiliza el nombre y la direccion del DTO real', () => {
  assert.equal(formatearDeposito({ nombre: 'Depósito de prueba', direccion: 'Calle ficticia 123' }, 3),
    'Depósito de prueba (Calle ficticia 123)');
  assert.equal(formatearDeposito({}, 3), 'Nombre no disponible (Dirección no disponible)');
  assert.equal(formatearDeposito(null, 3), 'Depósito #3 (datos no disponibles)');
  assert.equal(formatearDeposito(null), 'No especificado');
});

test('comercio y deposito no muestran transiciones administrativas', () => {
  for (const rol of ['COMERCIO', 'DEPOSITO', 'REPARTIDOR', undefined]) {
    for (const estado of ['LISTO_PARA_RETIRAR', 'ASIGNADO', 'RETIRADO', 'EN_CAMINO']) {
      const a = accionesPedido(rol, estado);
      assert.equal(a.asignar || a.retirar || a.viajar || a.entregar, false);
    }
  }
  for (const rol of ['ADMIN', 'COMERCIO', 'DEPOSITO']) {
    assert.equal(accionesPedido(rol, 'CREADO').preparar, true);
  }
});

test('los botones administrativos respetan el State y los estados finales no tienen acciones', () => {
  for (const [estado, accion] of [['LISTO_PARA_RETIRAR', 'asignar'], ['ASIGNADO', 'retirar'],
    ['RETIRADO', 'viajar'], ['EN_CAMINO', 'entregar']]) {
    assert.equal(accionesPedido('ADMIN', estado)[accion], true);
  }
  for (const estado of ['ENTREGADO', 'CANCELADO']) {
    for (const rol of ['ADMIN', 'COMERCIO', 'DEPOSITO', 'REPARTIDOR']) {
      assert.equal(Object.values(accionesPedido(rol, estado)).some(Boolean), false);
    }
  }
  assert.equal(accionesPedido('COMERCIO', 'RETIRADO').cancelar, true);
  assert.equal(accionesPedido('COMERCIO', 'EN_CAMINO').cancelar, false);
  assert.equal(accionesPedido('DEPOSITO', 'CREADO').cancelar, false);
});

test('una parada sin GPS no se presenta como una distancia cero conocida', () => {
  const r = resumenRuta({ puntoPartida: null, paradas: [{}], distanciaTotalKm: 0 });
  assert.equal(r.metricasDisponibles, false);
  assert.match(r.aviso, /ubicación/);
});

test('con GPS se conserva una distancia cero legitima y sin GPS se aclara el recorrido parcial', () => {
  assert.equal(resumenRuta({ puntoPartida: { lat: 0, lng: 0 }, paradas: [{}] }).metricasDisponibles, true);
  assert.equal(resumenRuta({ puntoPartida: { lat: 0, lng: 0 }, paradas: [{}] }).aviso, null);
  const parcial = resumenRuta({ puntoPartida: null, paradas: [{}, {}] });
  assert.equal(parcial.metricasDisponibles, true);
  assert.match(parcial.aviso, /no incluye/);
});
