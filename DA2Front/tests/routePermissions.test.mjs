import test from 'node:test';
import assert from 'node:assert/strict';
import { PERMISOS_RUTA, resolverAccesoRuta } from '../src/layout/routePermissions.js';

const acceso = (rol, ruta) => resolverAccesoRuta({ rol }, PERMISOS_RUTA[ruta]);

test('sin sesion o con rol desconocido se vuelve al login', () => {
  assert.equal(resolverAccesoRuta(null, ['ADMIN']).destino, '/');
  assert.equal(acceso('DESCONOCIDO', '/dashboard').destino, '/');
});

test('repartidor opera en su pantalla y no abre consultas o administracion genericas', () => {
  assert.equal(acceso('REPARTIDOR', '/mis-pedidos').permitido, true);
  for (const ruta of ['/pedidos', '/pedidos/:id', '/pedidos/nuevo', '/seguimiento',
    '/seguimiento/:id', '/repartidores', '/usuarios/:id', '/inventario', '/depositos/:id']) {
    assert.deepEqual(acceso('REPARTIDOR', ruta), { permitido: false, destino: '/dashboard' });
  }
});

test('deposito consulta pedidos e inventario pero no crea pedidos ni administra usuarios', () => {
  for (const ruta of ['/pedidos', '/pedidos/:id', '/inventario', '/seguimiento/:id']) {
    assert.equal(acceso('DEPOSITO', ruta).permitido, true);
  }
  for (const ruta of ['/pedidos/nuevo', '/usuarios', '/depositos/:id', '/mis-pedidos']) {
    assert.equal(acceso('DEPOSITO', ruta).permitido, false);
  }
});

test('comercio conserva alta de pedidos, productos y edicion propia, con ownership en backend', () => {
  for (const ruta of ['/pedidos/nuevo', '/productos', '/mi-comercio', '/mis-depositos', '/comercios/:id/editar']) {
    assert.equal(acceso('COMERCIO', ruta).permitido, true);
  }
  for (const ruta of ['/comercios', '/usuarios', '/repartidores/:id', '/depositos/:id']) {
    assert.equal(acceso('COMERCIO', ruta).permitido, false);
  }
});

test('admin conserva administracion y no suplanta las pantallas operativas de otro rol', () => {
  for (const ruta of ['/usuarios/:id', '/comercios/:id', '/depositos/:id', '/repartidores/:id', '/pedidos/nuevo']) {
    assert.equal(acceso('ADMIN', ruta).permitido, true);
  }
  assert.equal(acceso('ADMIN', '/mis-pedidos').permitido, false);
  assert.equal(acceso('ADMIN', '/mi-comercio').permitido, false);
});

test('una ruta sin permisos declarados falla cerrada y el dashboard nunca redirige sobre si mismo', () => {
  assert.equal(acceso('ADMIN', '/ruta-no-declarada').permitido, false);
  for (const rol of ['ADMIN', 'COMERCIO', 'DEPOSITO', 'REPARTIDOR']) {
    assert.equal(acceso(rol, '/dashboard').permitido, true);
  }
  assert.equal(Object.keys(PERMISOS_RUTA).length, 24);
});
