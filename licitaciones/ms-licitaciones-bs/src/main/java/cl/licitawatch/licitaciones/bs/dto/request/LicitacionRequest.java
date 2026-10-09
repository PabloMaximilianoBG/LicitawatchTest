package cl.licitawatch.licitaciones.bs.dto.request;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos que define el Licitador (condiciones y plazos). La imagen y el documento se suben aparte y su URL se genera sola. */
@Builder
public record LicitacionRequest(
        @NotBlank(message = "El título es obligatorio") @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres") String titulo,
        @NotBlank(message = "La descripción es obligatoria") @Size(min = 20, max = 5000, message = "La descripción debe tener entre 20 y 5000 caracteres") String descripcion,
        @NotNull(message = "Selecciona un rubro") Integer rubroId,
        @NotNull(message = "Selecciona una región") Integer regionId,
        @DecimalMin(value = "0", message = "El presupuesto no puede ser negativo") @Digits(integer = 13, fraction = 2) BigDecimal presupuestoMin,
        @DecimalMin(value = "0", message = "El presupuesto no puede ser negativo") @Digits(integer = 13, fraction = 2) BigDecimal presupuestoMax,
        @Min(value = 1, message = "Debe permitir al menos 1 postulante") @Max(1000) Integer maxPostulantes,
        @NotNull(message = "La fecha de cierre es obligatoria") LocalDate fechaCierre) {
}
