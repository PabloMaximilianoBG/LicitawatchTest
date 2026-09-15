package cl.licitawatch.asistente.contexto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LicitacionResponse(
        Long id,
        Long empresaId,
        String titulo,
        String rubro,
        BigDecimal montoEstimado,
        String region,
        String estado,
        LocalDate fechaCierre
) {
}
