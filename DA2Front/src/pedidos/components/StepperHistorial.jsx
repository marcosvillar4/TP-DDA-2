import { CheckCircle2 } from 'lucide-react';
import '../styles/StepperHistorial.css';

export default function StepperHistorial({ historial }) {
  return (
    <div className="stepper-container">
      <h3 className="stepper-title">Historial del envío</h3>
      <div className="stepper">
        {historial.map((step, index) => (
          <div key={index} className={`step step-${step.status}`}>
            <div className="step-indicator">
              {step.status === 'completed' ? (
                <CheckCircle2 size={18} className="step-icon-completed" />
              ) : (
                <div className="step-icon-dot" />
              )}
            </div>
            <div className="step-content">
              <div className="step-header">
                <span className="step-estado">{step.estado}</span>
                {step.status === 'current' && <span className="step-badge-actual">ACTUAL</span>}
              </div>
              {step.fecha && <span className="step-fecha">{step.fecha}</span>}
              {step.ubicacion && <span className="step-ubicacion">{step.ubicacion}</span>}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
