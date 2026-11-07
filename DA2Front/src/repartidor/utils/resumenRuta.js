export function resumenRuta(ruta) {
  const paradas = ruta?.paradas?.length ?? 0;
  const tieneUbicacion = Number.isFinite(ruta?.puntoPartida?.lat)
    && Number.isFinite(ruta?.puntoPartida?.lng);
  return {
    metricasDisponibles: tieneUbicacion || paradas >= 2,
    aviso: tieneUbicacion || paradas === 0 ? null
      : paradas === 1
        ? 'Para calcular la distancia y el tiempo hasta la parada, usá «Usar mi ubicación».'
        : 'Distancia entre paradas: no incluye el tramo desde tu ubicación hasta la primera parada.',
  };
}
