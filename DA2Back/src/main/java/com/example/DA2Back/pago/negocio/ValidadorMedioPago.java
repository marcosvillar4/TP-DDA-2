package com.example.DA2Back.pago.negocio;

import java.time.YearMonth;
import java.util.regex.Pattern;

/**
 * Validaciones de los datos del medio de pago. Clase pura (sin Spring) para
 * poder testearla de forma aislada. Todas las fallas lanzan
 * IllegalArgumentException, que el GlobalExceptionHandler responde con 400.
 */
public final class ValidadorMedioPago {

    private static final Pattern SOLO_DIGITOS = Pattern.compile("\\d+");
    private static final Pattern VENCIMIENTO = Pattern.compile("^(0[1-9]|1[0-2])/(\\d{2}|\\d{4})$");
    private static final Pattern CVV = Pattern.compile("^\\d{3,4}$");
    private static final Pattern TITULAR = Pattern.compile("^\\p{L}[\\p{L} .'-]{2,59}$");
    private static final int MAX_ANIOS_VIGENCIA = 20;

    private static final int[] PESOS_CBU_BLOQUE_1 = {7, 1, 3, 9, 7, 1, 3};
    private static final int[] PESOS_CBU_BLOQUE_2 = {3, 9, 7, 1, 3, 9, 7, 1, 3, 9, 7, 1, 3};

    private ValidadorMedioPago() {
    }

    /**
     * Valida los datos de tarjeta y devuelve el numero normalizado (solo digitos).
     */
    public static String validarTarjeta(String numero, String titular, String vencimiento,
                                        String cvv, YearMonth actual) {
        String digitos = normalizarNumeroTarjeta(numero);
        if (!superaLuhn(digitos)) {
            throw new IllegalArgumentException("El numero de tarjeta es invalido");
        }
        if (titular == null || !TITULAR.matcher(titular.trim()).matches()) {
            throw new IllegalArgumentException("El titular de la tarjeta es invalido");
        }
        validarVencimiento(vencimiento, actual);
        if (cvv == null || !CVV.matcher(cvv).matches()) {
            throw new IllegalArgumentException("El codigo de seguridad debe tener 3 o 4 digitos");
        }
        return digitos;
    }

    /** Valida un CBU (22 digitos y digitos verificadores) y lo devuelve normalizado. */
    public static String validarCbu(String cbu) {
        if (cbu == null || cbu.isBlank()) {
            throw new IllegalArgumentException("El CBU es obligatorio para transferencias");
        }
        String limpio = cbu.trim();
        if (limpio.length() != 22 || !SOLO_DIGITOS.matcher(limpio).matches()) {
            throw new IllegalArgumentException("El CBU debe tener 22 digitos numericos");
        }
        if (!digitoVerificadorCbuOk(limpio.substring(0, 8), PESOS_CBU_BLOQUE_1)
                || !digitoVerificadorCbuOk(limpio.substring(8), PESOS_CBU_BLOQUE_2)) {
            throw new IllegalArgumentException("El CBU es invalido (digito verificador incorrecto)");
        }
        return limpio;
    }

    static String normalizarNumeroTarjeta(String numero) {
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("El numero de tarjeta es obligatorio");
        }
        String digitos = numero.replaceAll("[\\s-]", "");
        if (!SOLO_DIGITOS.matcher(digitos).matches() || digitos.length() < 13 || digitos.length() > 19) {
            throw new IllegalArgumentException("El numero de tarjeta debe tener entre 13 y 19 digitos");
        }
        return digitos;
    }

    static void validarVencimiento(String vencimiento, YearMonth actual) {
        if (vencimiento == null || !VENCIMIENTO.matcher(vencimiento.trim()).matches()) {
            throw new IllegalArgumentException("El vencimiento debe tener formato MM/AA");
        }
        String[] partes = vencimiento.trim().split("/");
        int mes = Integer.parseInt(partes[0]);
        int anio = Integer.parseInt(partes[1]);
        if (partes[1].length() == 2) {
            anio += 2000;
        }
        YearMonth vence = YearMonth.of(anio, mes);
        if (vence.isBefore(actual)) {
            throw new IllegalArgumentException("La tarjeta esta vencida");
        }
        if (vence.isAfter(actual.plusYears(MAX_ANIOS_VIGENCIA))) {
            throw new IllegalArgumentException("La fecha de vencimiento es invalida");
        }
    }

    /** Algoritmo de Luhn. */
    static boolean superaLuhn(String digitos) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int d = digitos.charAt(i) - '0';
            if (duplicar) {
                d *= 2;
                if (d > 9) {
                    d -= 9;
                }
            }
            suma += d;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }

    /** El ultimo digito del bloque es el verificador de los anteriores. */
    private static boolean digitoVerificadorCbuOk(String bloque, int[] pesos) {
        int suma = 0;
        for (int i = 0; i < pesos.length; i++) {
            suma += (bloque.charAt(i) - '0') * pesos[i];
        }
        int verificador = (10 - (suma % 10)) % 10;
        return verificador == bloque.charAt(bloque.length() - 1) - '0';
    }
}
