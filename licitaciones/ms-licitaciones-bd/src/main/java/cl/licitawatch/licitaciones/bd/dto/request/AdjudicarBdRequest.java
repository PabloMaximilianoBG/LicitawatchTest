package cl.licitawatch.licitaciones.bd.dto.request;

import jakarta.validation.constraints.NotNull;

public record AdjudicarBdRequest(@NotNull Integer postulacionId) {
}
