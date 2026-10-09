package cl.licitawatch.ventas.bs.client.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SuscripcionBdDto(Integer id, Integer usuarioId, String plan, String estado, LocalDate fechaUltimoPagoAprobado,
                               boolean tieneVentaSinPago) {
}
