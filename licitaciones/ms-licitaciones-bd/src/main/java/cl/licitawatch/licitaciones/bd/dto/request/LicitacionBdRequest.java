package cl.licitawatch.licitaciones.bd.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Columnas editables de licitacion. estado = nombre del catálogo estado_licitacion. */
@Builder
public record LicitacionBdRequest(@NotNull Integer licitadorId, @NotBlank String titulo, @NotBlank String descripcion,
                                  @NotNull Integer rubroId, @NotNull Integer regionId, BigDecimal presupuestoMin,
                                  BigDecimal presupuestoMax, Integer maxPostulantes, @NotNull LocalDate fechaCierre,
                                  String estado) {
}
