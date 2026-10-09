package cl.licitawatch.licitaciones.bs.dto.response;

import java.util.List;

public record CatalogosResponse(List<String> estadosLicitacion, List<String> estadosPostulacion, List<String> tiposArchivo) {
}
