package cl.licitawatch.ventas.dto;

import java.math.BigDecimal;

public record PlanResponse(
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
