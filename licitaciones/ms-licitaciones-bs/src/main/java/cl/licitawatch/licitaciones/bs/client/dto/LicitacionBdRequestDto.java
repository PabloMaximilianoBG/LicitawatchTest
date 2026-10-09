package cl.licitawatch.licitaciones.bs.client.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LicitacionBdRequestDto(Integer licitadorId, String titulo, String descripcion, Integer rubroId, Integer regionId,
                                     BigDecimal presupuestoMin, BigDecimal presupuestoMax, Integer maxPostulantes,
                                     LocalDate fechaCierre, String estado) {
}
