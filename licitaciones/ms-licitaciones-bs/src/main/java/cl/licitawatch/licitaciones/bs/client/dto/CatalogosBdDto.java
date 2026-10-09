package cl.licitawatch.licitaciones.bs.client.dto;

import java.util.List;

public record CatalogosBdDto(List<String> estadosLicitacion, List<String> estadosPostulacion, List<String> tiposArchivo) {
}
