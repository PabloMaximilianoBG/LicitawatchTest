package cl.licitawatch.chat.bs.client.dto;

public record ContextoPostulacionDto(Integer postulacionId, String estado, Integer licitacionId, String licitacionTitulo,
                                     Integer licitadorId, Integer pymeId) {
}
