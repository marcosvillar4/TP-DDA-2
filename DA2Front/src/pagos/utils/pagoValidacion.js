/**
 * Validaciones del formulario de pago. Replican las del backend
 * (ValidadorMedioPago) para dar feedback inmediato; el servidor vuelve a
 * validar todo, esto es solo UX.
 */

export const METODOS_PAGO = [
  { value: 'TARJETA_CREDITO', label: 'Tarjeta de crédito' },
  { value: 'TARJETA_DEBITO', label: 'Tarjeta de débito' },
  { value: 'TRANSFERENCIA', label: 'Transferencia (CBU)' },
];

export const esTarjeta = (metodo) => metodo === 'TARJETA_CREDITO' || metodo === 'TARJETA_DEBITO';

export function soloDigitos(valor) {
  return String(valor ?? '').replace(/[\s-]/g, '');
}

/** Algoritmo de Luhn sobre una cadena de dígitos. */
export function superaLuhn(digitos) {
  if (!/^\d+$/.test(digitos)) return false;
  let suma = 0;
  let duplicar = false;
  for (let i = digitos.length - 1; i >= 0; i--) {
    let d = Number(digitos[i]);
    if (duplicar) {
      d *= 2;
      if (d > 9) d -= 9;
    }
    suma += d;
    duplicar = !duplicar;
  }
  return suma % 10 === 0;
}

const PESOS_CBU_1 = [7, 1, 3, 9, 7, 1, 3];
const PESOS_CBU_2 = [3, 9, 7, 1, 3, 9, 7, 1, 3, 9, 7, 1, 3];

function bloqueCbuOk(bloque, pesos) {
  const suma = pesos.reduce((acc, peso, i) => acc + Number(bloque[i]) * peso, 0);
  return (10 - (suma % 10)) % 10 === Number(bloque[bloque.length - 1]);
}

export function cbuValido(cbu) {
  const limpio = String(cbu ?? '').trim();
  return /^\d{22}$/.test(limpio)
    && bloqueCbuOk(limpio.slice(0, 8), PESOS_CBU_1)
    && bloqueCbuOk(limpio.slice(8), PESOS_CBU_2);
}

/** Devuelve null si el vencimiento (MM/AA o MM/AAAA) es válido y no está vencido. */
export function errorVencimiento(valor, hoy = new Date()) {
  const m = /^(0[1-9]|1[0-2])\/(\d{2}|\d{4})$/.exec(String(valor ?? '').trim());
  if (!m) return 'Usá el formato MM/AA';
  const mes = Number(m[1]);
  const anio = m[2].length === 2 ? 2000 + Number(m[2]) : Number(m[2]);
  const indice = anio * 12 + mes;
  const indiceHoy = hoy.getFullYear() * 12 + (hoy.getMonth() + 1);
  if (indice < indiceHoy) return 'La tarjeta está vencida';
  if (indice > indiceHoy + 20 * 12) return 'La fecha de vencimiento es inválida';
  return null;
}

/**
 * @param {string} metodo   TARJETA_CREDITO | TARJETA_DEBITO | TRANSFERENCIA
 * @param {object} datos    { numeroTarjeta, titular, vencimiento, cvv, cbu }
 * @returns {Object<string,string>} errores por campo; vacío si todo está bien
 */
export function validarFormularioPago(metodo, datos, hoy = new Date()) {
  const errores = {};
  if (!METODOS_PAGO.some((m) => m.value === metodo)) {
    errores.metodoPago = 'Elegí un método de pago';
    return errores;
  }

  if (esTarjeta(metodo)) {
    const numero = soloDigitos(datos.numeroTarjeta);
    if (!numero) errores.numeroTarjeta = 'Ingresá el número de tarjeta';
    else if (!/^\d{13,19}$/.test(numero)) errores.numeroTarjeta = 'El número debe tener entre 13 y 19 dígitos';
    else if (!superaLuhn(numero)) errores.numeroTarjeta = 'El número de tarjeta es inválido';

    if (!/^\p{L}[\p{L} .'-]{2,59}$/u.test(String(datos.titular ?? '').trim())) {
      errores.titular = 'Ingresá el nombre del titular tal como figura en la tarjeta';
    }

    const errVenc = errorVencimiento(datos.vencimiento, hoy);
    if (errVenc) errores.vencimiento = errVenc;

    if (!/^\d{3,4}$/.test(String(datos.cvv ?? ''))) errores.cvv = 'El código debe tener 3 o 4 dígitos';
  } else if (!String(datos.cbu ?? '').trim()) {
    errores.cbu = 'Ingresá el CBU';
  } else if (!/^\d{22}$/.test(String(datos.cbu).trim())) {
    errores.cbu = 'El CBU debe tener 22 dígitos';
  } else if (!cbuValido(datos.cbu)) {
    errores.cbu = 'El CBU es inválido (dígito verificador incorrecto)';
  }

  return errores;
}

/** Arma el body del POST /pagar enviando solo los campos del método elegido. */
export function armarBodyPago(metodo, datos, monto) {
  if (esTarjeta(metodo)) {
    return {
      metodoPago: metodo,
      monto,
      numeroTarjeta: soloDigitos(datos.numeroTarjeta),
      titular: datos.titular.trim(),
      vencimiento: datos.vencimiento.trim(),
      cvv: datos.cvv,
    };
  }
  return { metodoPago: metodo, monto, cbu: datos.cbu.trim() };
}

/** Agrupa de a 4 dígitos mientras se tipea: 4242424242424242 -> 4242 4242 4242 4242 */
export function formatearNumeroTarjeta(valor) {
  return soloDigitos(valor).replace(/\D/g, '').slice(0, 19).replace(/(.{4})/g, '$1 ').trim();
}
