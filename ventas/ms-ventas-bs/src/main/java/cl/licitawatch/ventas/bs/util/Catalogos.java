package cl.licitawatch.ventas.bs.util;

import java.time.LocalDate;
import java.time.ZoneId;

/** Valores de los catálogos del ER de ventas-bd. */
public final class Catalogos {
    public static final String ESTANDAR = "Estándar";
    public static final String PREMIUM = "Premium";
    public static final String ACTIVA = "Activa";
    public static final String VENCIDA = "Vencida";
    public static final String CANCELADA = "Cancelada";
    public static final String APROBADO = "Aprobado";
    public static final String RECHAZADO = "Rechazado";
    public static final String PENDIENTE = "Pendiente";
    public static final String NOTIF_PAGO_CONFIRMADO = "Pago confirmado";
    public static final ZoneId CHILE = ZoneId.of("America/Santiago");

    private Catalogos() {
    }

    public static LocalDate hoy() {
        return LocalDate.now(CHILE);
    }
}
