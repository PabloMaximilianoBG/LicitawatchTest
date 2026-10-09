package cl.licitawatch.licitaciones.bs.dto.response;

/** Uso interno (MS.chat.bs): datos para validar que la conversación corresponde a una postulación aprobada. */
public record ContextoPostulacionResponse(Integer postulacionId, String estado, Integer licitacionId, String licitacionTitulo,
                                          Integer licitadorId, Integer pymeId) {
}
