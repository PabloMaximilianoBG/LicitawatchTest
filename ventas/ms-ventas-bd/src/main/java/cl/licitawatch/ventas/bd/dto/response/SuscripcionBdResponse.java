package cl.licitawatch.ventas.bd.dto.response;

import lombok.Builder;

import java.time.LocalDate;

/** fechaUltimoPagoAprobado = fecha de la última venta pagada (base del cálculo de vigencia de Premium). */
@Builder
public record SuscripcionBdResponse(Integer id, Integer usuarioId, String plan, String estado, LocalDate fechaUltimoPagoAprobado,
                                    boolean tieneVentaSinPago) {
}
