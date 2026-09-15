package cl.licitawatch.asistente.contexto;

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
