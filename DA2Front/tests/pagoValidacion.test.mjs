import test from 'node:test';
import assert from 'node:assert/strict';
import {
  superaLuhn, cbuValido, errorVencimiento, validarFormularioPago, armarBodyPago, formatearNumeroTarjeta,
} from '../src/pagos/utils/pagoValidacion.js';
import { PERMISOS_RUTA, resolverAccesoRuta } from '../src/layout/routePermissions.js';

const HOY = new Date(2026, 10, 7); // 7/nov/2026
const CBU_OK = '0170099220000000123453';
const tarjetaOk = { numeroTarjeta: '4242 4242 4242 4242', titular: 'Juan Perez', vencimiento: '12/28', cvv: '123', cbu: '' };

test('luhn acepta tarjetas de prueba y rechaza numeros alterados', () => {
  for (const n of ['4242424242424242', '4000000000009995', '4000000000000002']) assert.equal(superaLuhn(n), true);
  assert.equal(superaLuhn('4242424242424241'), false);
  assert.equal(superaLuhn('42a2'), false);
});

test('cbu: valida largo y digitos verificadores', () => {
  assert.equal(cbuValido(CBU_OK), true);
  assert.equal(cbuValido(CBU_OK.slice(0, 21) + '4'), false);
  assert.equal(cbuValido('123'), false);
  assert.equal(cbuValido(null), false);
});

test('vencimiento: formato, vencida, vigente este mes y demasiado lejana', () => {
  assert.equal(errorVencimiento('11/26', HOY), null);
  assert.equal(errorVencimiento('12/2028', HOY), null);
  assert.match(errorVencimiento('10/26', HOY), /vencida/);
  assert.match(errorVencimiento('13/28', HOY), /MM\/AA/);
  assert.match(errorVencimiento('01/60', HOY), /inválida/);
});

test('formulario de tarjeta valido no tiene errores', () => {
  assert.deepEqual(validarFormularioPago('TARJETA_CREDITO', tarjetaOk, HOY), {});
  assert.deepEqual(validarFormularioPago('TARJETA_DEBITO', tarjetaOk, HOY), {});
});

test('formulario de tarjeta informa cada campo invalido', () => {
  const errores = validarFormularioPago('TARJETA_CREDITO',
    { numeroTarjeta: '4242 4242 4242 4241', titular: 'J1', vencimiento: '01/20', cvv: '12', cbu: '' }, HOY);
  assert.deepEqual(Object.keys(errores).sort(), ['cvv', 'numeroTarjeta', 'titular', 'vencimiento']);
});

test('transferencia exige un CBU valido y no pide datos de tarjeta', () => {
  assert.deepEqual(validarFormularioPago('TRANSFERENCIA', { cbu: CBU_OK }, HOY), {});
  assert.ok(validarFormularioPago('TRANSFERENCIA', { cbu: '' }, HOY).cbu);
  assert.ok(validarFormularioPago('TRANSFERENCIA', { cbu: '1234' }, HOY).cbu);
  assert.ok(validarFormularioPago('TRANSFERENCIA', { cbu: CBU_OK.slice(0, 21) + '4' }, HOY).cbu);
});

test('metodo desconocido se rechaza', () => {
  assert.ok(validarFormularioPago('EFECTIVO', {}, HOY).metodoPago);
});

test('el body solo envia los campos del metodo elegido y sin espacios en la tarjeta', () => {
  const tarjeta = armarBodyPago('TARJETA_CREDITO', tarjetaOk, 5000);
  assert.equal(tarjeta.numeroTarjeta, '4242424242424242');
  assert.equal(tarjeta.cbu, undefined);
  const transf = armarBodyPago('TRANSFERENCIA', { ...tarjetaOk, cbu: CBU_OK }, 5000);
  assert.deepEqual(Object.keys(transf).sort(), ['cbu', 'metodoPago', 'monto']);
});

test('formatea el numero de tarjeta de a 4 digitos', () => {
  assert.equal(formatearNumeroTarjeta('4242424242424242'), '4242 4242 4242 4242');
  assert.equal(formatearNumeroTarjeta('42ab42'), '4242');
});

test('/pagos: lo ven comercio y repartidor; deposito y admin no (no tienen pagos propios)', () => {
  const acceso = (rol) => resolverAccesoRuta({ rol }, PERMISOS_RUTA['/pagos']).permitido;
  assert.equal(acceso('COMERCIO'), true);
  assert.equal(acceso('REPARTIDOR'), true);
  assert.equal(acceso('DEPOSITO'), false);
  assert.equal(acceso('ADMIN'), false);
});
