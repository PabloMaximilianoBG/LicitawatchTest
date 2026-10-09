package cl.licitawatch.licitaciones.bs.dto.response;

/** Uso del límite mensual de postulaciones del plan (Estándar 3, Premium 7 — PPT diap. 7). */
public record UsoPlanResponse(String plan, boolean premium, long postulacionesMes, Integer limitePostulacionesMes, long restantes) {
}
