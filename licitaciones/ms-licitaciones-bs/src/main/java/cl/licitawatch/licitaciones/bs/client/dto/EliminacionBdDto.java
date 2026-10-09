package cl.licitawatch.licitaciones.bs.client.dto;

import java.util.List;

public record EliminacionBdDto(Integer licitacionId, List<Integer> postulacionesEliminadas) {
}
