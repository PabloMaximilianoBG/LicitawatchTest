package cl.licitawatch.licitaciones.bd.dto.response;

import java.util.List;

public record EliminacionBdResponse(Integer licitacionId, List<Integer> postulacionesEliminadas) {
}
