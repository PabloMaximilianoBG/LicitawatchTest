package cl.licitawatch.licitaciones.bd.dto.request;

import lombok.Builder;

/** Actualiza imagen y/o documento. Los flags limpiar* dejan las columnas en NULL. */
@Builder
public record ArchivosBdRequest(String imagenUrl, boolean limpiarImagen, String archivoUrl, String archivoNombre,
                                String tipoArchivo, boolean limpiarArchivo) {
}
