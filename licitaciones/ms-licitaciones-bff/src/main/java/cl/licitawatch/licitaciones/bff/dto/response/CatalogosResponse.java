package cl.licitawatch.licitaciones.bff.dto.response;

import java.util.List;

public record CatalogosResponse(List<String> estadosLicitacion, List<String> estadosPostulacion, List<String> tiposArchivo) {
}
