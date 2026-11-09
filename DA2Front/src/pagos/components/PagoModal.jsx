import { useState } from 'react';
import { X, Lock } from 'lucide-react';
import { pagarPago } from '../../../api/pagosApi';
import { formatMoneda } from '../../pedidos/utils/formatMoneda';
import {
  METODOS_PAGO,
  esTarjeta,
  validarFormularioPago,
  armarBodyPago,
  formatearNumeroTarjeta,
} from '../utils/pagoValidacion';
import '../styles/Pagos.css';

const FORM_INICIAL = { numeroTarjeta: '', titular: '', vencimiento: '', cvv: '', cbu: '' };

/**
 * Modal de pago simulado. No se envía nada a un procesador real: el backend
 * valida los datos y una pasarela simulada aprueba o rechaza.
 */
export default function PagoModal({ pago, onClose, onPagado }) {
  const [metodo, setMetodo] = useState('TARJETA_CREDITO');
  const [datos, setDatos] = useState(FORM_INICIAL);
  const [errores, setErrores] = useState({});
  const [errorServidor, setErrorServidor] = useState('');
  const [enviando, setEnviando] = useState(false);

  const cambiar = (campo) => (e) => {
    const valor = campo === 'numeroTarjeta' ? formatearNumeroTarjeta(e.target.value) : e.target.value;
    setDatos((prev) => ({ ...prev, [campo]: valor }));
    setErrores((prev) => ({ ...prev, [campo]: undefined }));
    setErrorServidor('');
  };

  const enviar = async (e) => {
    e.preventDefault();
    if (enviando) return;

    const errs = validarFormularioPago(metodo, datos);
    setErrores(errs);
    if (Object.keys(errs).length > 0) return;

    setEnviando(true);
    setErrorServidor('');
    try {
      const resultado = await pagarPago(pago.id, armarBodyPago(metodo, datos, pago.monto));
      onPagado(resultado);
    } catch (err) {
      setErrorServidor(err.message || 'No se pudo procesar el pago.');
      setEnviando(false);
    }
  };

  const campo = (id, label, props = {}) => (
    <div className="pagos-field">
      <label htmlFor={`pago-${id}`}>{label}</label>
      <input
        id={`pago-${id}`}
        value={datos[id]}
        onChange={cambiar(id)}
        disabled={enviando}
        aria-invalid={Boolean(errores[id])}
        className={errores[id] ? 'pagos-input pagos-input-error' : 'pagos-input'}
        {...props}
      />
      {errores[id] && <span className="pagos-field-error">{errores[id]}</span>}
    </div>
  );

  return (
    <div className="pagos-overlay" role="dialog" aria-modal="true" aria-labelledby="pago-modal-titulo">
      <form className="pagos-modal" onSubmit={enviar} noValidate>
        <div className="pagos-modal-header">
          <h2 id="pago-modal-titulo">Pagar envío del pedido LOG-{pago.pedidoId}</h2>
          <button type="button" className="pagos-icon-btn" onClick={onClose} disabled={enviando} aria-label="Cerrar">
            <X size={18} />
          </button>
        </div>

        <div className="pagos-resumen">
          <span>Importe a pagar</span>
          <strong>{formatMoneda(pago.monto)}</strong>
          <small>{pago.direccionDestino}</small>
        </div>

        <div className="pagos-simulado">
          <Lock size={14} />
          <span>
            Pago simulado: no se realiza ningún cargo. Tarjeta aprobada <code>4242 4242 4242 4242</code>;
            rechazos de prueba: <code>4000 0000 0000 9995</code> (sin fondos) y <code>4000 0000 0000 0002</code>.
          </span>
        </div>

        <div className="pagos-field">
          <label htmlFor="pago-metodo">Método de pago</label>
          <select
            id="pago-metodo"
            className="pagos-input"
            value={metodo}
            disabled={enviando}
            onChange={(e) => { setMetodo(e.target.value); setErrores({}); setErrorServidor(''); }}
          >
            {METODOS_PAGO.map((m) => <option key={m.value} value={m.value}>{m.label}</option>)}
          </select>
        </div>

        {esTarjeta(metodo) ? (
          <>
            {campo('numeroTarjeta', 'Número de tarjeta', { inputMode: 'numeric', autoComplete: 'off', placeholder: '0000 0000 0000 0000', maxLength: 23 })}
            {campo('titular', 'Titular', { autoComplete: 'off', placeholder: 'Como figura en la tarjeta', maxLength: 60 })}
            <div className="pagos-row">
              {campo('vencimiento', 'Vencimiento', { autoComplete: 'off', placeholder: 'MM/AA', maxLength: 7 })}
              {campo('cvv', 'CVV', { type: 'password', inputMode: 'numeric', autoComplete: 'off', placeholder: '123', maxLength: 4 })}
            </div>
          </>
        ) : (
          campo('cbu', 'CBU de origen', { inputMode: 'numeric', autoComplete: 'off', placeholder: '22 dígitos', maxLength: 22 })
        )}

        {errorServidor && <div className="pagos-error-servidor" role="alert">{errorServidor}</div>}

        <div className="pagos-modal-actions">
          <button type="button" className="pagos-btn-secundario" onClick={onClose} disabled={enviando}>Cancelar</button>
          <button type="submit" className="pagos-btn-primario" disabled={enviando}>
            {enviando ? 'Procesando…' : `Pagar ${formatMoneda(pago.monto)}`}
          </button>
        </div>
      </form>
    </div>
  );
}
