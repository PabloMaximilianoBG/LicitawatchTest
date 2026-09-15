package cl.licitawatch.asistente.contexto;

import java.math.BigDecimal;

public record PlanCatalogoResponse(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        Integer limitePublicacionesMes,
        Integer limitePostulacionesMes,
        boolean soportePrioritario,
        boolean notificacionesAutomaticas
) {
}
