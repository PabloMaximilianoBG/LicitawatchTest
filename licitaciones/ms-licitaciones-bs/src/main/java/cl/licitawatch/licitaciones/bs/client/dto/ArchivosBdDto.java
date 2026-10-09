package cl.licitawatch.licitaciones.bs.client.dto;

import lombok.Builder;

@Builder
public record ArchivosBdDto(String imagenUrl, boolean limpiarImagen, String archivoUrl, String archivoNombre, String tipoArchivo,
                            boolean limpiarArchivo) {
}
