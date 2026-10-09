package cl.licitawatch.ventas.bs.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record PlanResponse(Integer id, String nombre, BigDecimal precio, Integer vigenciaDias, int limitePostulacionesMes,
                           boolean accesoLicitasist, boolean prioridadVisibilidad, String soporte, List<String> beneficios) {
}
