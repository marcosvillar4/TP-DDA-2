import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import "./styles/MapaRuta.css";

const CENTRO_POR_DEFECTO = [-34.6037, -58.3816]; // Buenos Aires
const COLOR_TIPO = { RETIRO: "#8b5cf6", ENTREGA: "#10b981" };

function marcadorNumerado(texto, color) {
  return L.divIcon({
    className: "mapa-ruta-marker-wrapper",
    html: `<span class="mapa-ruta-marker" style="background:${color}">${texto}</span>`,
    iconSize: [28, 28],
    iconAnchor: [14, 14],
  });
}

/** Tooltip armado con nodos de texto (las direcciones vienen del usuario: nada de HTML). */
function tooltip(titulo, detalle) {
  const el = document.createElement("div");
  const strong = document.createElement("strong");
  strong.textContent = titulo;
  el.appendChild(strong);
  if (detalle) {
    el.appendChild(document.createElement("br"));
    el.appendChild(document.createTextNode(detalle));
  }
  return el;
}

/**
 * Mapa gratuito (Leaflet + tiles de OpenStreetMap) que dibuja un recorrido.
 * `ruta` es el RutaDTO del backend: { puntoPartida, paradas, geometria }.
 */
export default function MapaRuta({ ruta, altura = 340 }) {
  const contenedorRef = useRef(null);
  const mapaRef = useRef(null);
  const capasRef = useRef(null);

  useEffect(() => {
    const mapa = L.map(contenedorRef.current, { scrollWheelZoom: false })
      .setView(CENTRO_POR_DEFECTO, 12);
    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; Colaboradores de OpenStreetMap",
    }).addTo(mapa);
    capasRef.current = L.layerGroup().addTo(mapa);
    mapaRef.current = mapa;
    const t = setTimeout(() => mapa.invalidateSize(), 0);
    return () => {
      clearTimeout(t);
      mapa.remove();
      mapaRef.current = null;
      capasRef.current = null;
    };
  }, []);

  useEffect(() => {
    const mapa = mapaRef.current;
    const capas = capasRef.current;
    if (!mapa || !capas) return;
    capas.clearLayers();
    if (!ruta) return;

    const puntos = [];

    if (Array.isArray(ruta.geometria) && ruta.geometria.length > 1) {
      L.polyline(ruta.geometria, { color: "#16223f", weight: 5, opacity: 0.75 }).addTo(capas);
      ruta.geometria.forEach((p) => puntos.push(p));
    }

    if (ruta.puntoPartida?.lat != null) {
      const p = [ruta.puntoPartida.lat, ruta.puntoPartida.lng];
      L.marker(p, { icon: marcadorNumerado("●", "#3b82f6") })
        .bindTooltip(tooltip("Tu ubicación"))
        .addTo(capas);
      puntos.push(p);
    }

    (ruta.paradas || []).forEach((parada) => {
      const p = [parada.lat, parada.lng];
      const tipo = parada.tipo === "RETIRO" ? "Retiro" : "Entrega";
      L.marker(p, { icon: marcadorNumerado(parada.orden, COLOR_TIPO[parada.tipo] || "#475569") })
        .bindTooltip(tooltip(`${parada.orden}. ${tipo} · pedido #${parada.pedidoId}`, parada.direccion))
        .addTo(capas);
      puntos.push(p);
    });

    if (puntos.length > 0) {
      mapa.fitBounds(L.latLngBounds(puntos), { padding: [30, 30], maxZoom: 16 });
    }
  }, [ruta]);

  return <div ref={contenedorRef} className="mapa-ruta" style={{ height: altura }} />;
}
