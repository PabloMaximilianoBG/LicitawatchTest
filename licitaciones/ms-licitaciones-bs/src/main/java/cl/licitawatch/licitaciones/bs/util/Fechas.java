package cl.licitawatch.licitaciones.bs.util;

import java.time.LocalDate;
import java.time.ZoneId;

public final class Fechas {
    public static final ZoneId CHILE = ZoneId.of("America/Santiago");

    private Fechas() {
    }

    public static LocalDate hoy() {
        return LocalDate.now(CHILE);
    }
}
