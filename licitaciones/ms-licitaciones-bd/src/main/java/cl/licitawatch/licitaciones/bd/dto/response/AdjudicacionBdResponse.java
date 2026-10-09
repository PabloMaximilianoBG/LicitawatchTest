package cl.licitawatch.licitaciones.bd.dto.response;

import java.util.List;

public record AdjudicacionBdResponse(PostulacionBdResponse aprobada, List<PostulacionBdResponse> rechazadas) {
}
