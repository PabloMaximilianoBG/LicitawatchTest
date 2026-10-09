package cl.licitawatch.licitaciones.bs.client.dto;

import java.util.List;

public record AdjudicacionBdDto(PostulacionBdDto aprobada, List<PostulacionBdDto> rechazadas) {
}
