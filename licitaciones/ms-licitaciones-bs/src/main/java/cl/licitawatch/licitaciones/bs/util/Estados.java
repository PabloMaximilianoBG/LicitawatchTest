package cl.licitawatch.licitaciones.bs.util;

/** Valores de los catálogos del ER (estado_licitacion, estado_postulacion, tipo_notificacion). */
public final class Estados {
    public static final String ABIERTA = "Abierta";
    public static final String CERRADA = "Cerrada";
    public static final String ADJUDICADA = "Adjudicada";
    public static final String PENDIENTE = "Pendiente";
    public static final String APROBADA = "Aprobada";
    public static final String RECHAZADA = "Rechazada";

    public static final String NOTIF_LICITACION_PUBLICADA = "Licitación publicada";
    public static final String NOTIF_POSTULACION_RECIBIDA = "Postulación recibida";
    public static final String NOTIF_POSTULACION_APROBADA = "Postulación aprobada";
    public static final String NOTIF_POSTULACION_RECHAZADA = "Postulación rechazada";

    private Estados() {
    }
}
