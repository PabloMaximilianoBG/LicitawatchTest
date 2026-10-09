package cl.licitawatch.licitaciones.bd.dto.response;

import java.util.List;

public record CatalogosBdResponse(List<String> estadosLicitacion, List<String> estadosPostulacion, List<String> tiposArchivo) {
}
