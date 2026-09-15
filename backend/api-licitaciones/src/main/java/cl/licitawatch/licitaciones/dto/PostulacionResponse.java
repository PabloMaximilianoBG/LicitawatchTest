package cl.licitawatch.licitaciones.dto;

import java.time.LocalDate;

public record PostulacionResponse(
        Long id,
        Long licitacionId,
        String licitacionTitulo,
        Long clienteId,
        LocalDate fechaPostulacion,
        String propuesta,
        String estado
) {
}
