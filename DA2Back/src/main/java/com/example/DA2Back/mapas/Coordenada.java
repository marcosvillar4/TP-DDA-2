package com.example.DA2Back.mapas;

/** Punto geografico (WGS84). */
public record Coordenada(double lat, double lng) {

    private static final double RADIO_TIERRA_KM = 6371.0088;

    /** Distancia en linea recta (formula de Haversine) en kilometros. */
    public static double distanciaKm(Coordenada a, Coordenada b) {
        double dLat = Math.toRadians(b.lat - a.lat);
        double dLng = Math.toRadians(b.lng - a.lng);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.lat)) * Math.cos(Math.toRadians(b.lat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.min(1.0, Math.sqrt(h)));
    }

    public boolean esValida() {
        return !Double.isNaN(lat) && !Double.isNaN(lng)
                && lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }
}
