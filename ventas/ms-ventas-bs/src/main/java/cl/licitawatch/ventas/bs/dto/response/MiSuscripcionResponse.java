package cl.licitawatch.ventas.bs.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record MiSuscripcionResponse(Integer suscripcionId, String plan, String estado, boolean premium, LocalDate fechaInicio,
                                    LocalDate fechaVencimiento, Long diasRestantes, int limitePostulacionesMes,
                                    boolean accesoLicitasist, String soporte, BigDecimal precioPremium,
                                    VentaResponse ventaPendiente, List<String> beneficios) {
}
