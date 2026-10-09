package cl.licitawatch.licitaciones.bd.dto.request;

import jakarta.validation.constraints.NotNull;

public record PostulacionBdRequest(@NotNull Integer licitacionId, @NotNull Integer pymeId, String mensaje) {
}
