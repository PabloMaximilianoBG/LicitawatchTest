package cl.licitawatch.licitaciones.dto;

import jakarta.validation.constraints.NotNull;
import cl.licitawatch.licitaciones.entity.EstadoPostulacion;

public record ActualizarEstadoPostulacionRequest(
        @NotNull EstadoPostulacion estado
) {
}
