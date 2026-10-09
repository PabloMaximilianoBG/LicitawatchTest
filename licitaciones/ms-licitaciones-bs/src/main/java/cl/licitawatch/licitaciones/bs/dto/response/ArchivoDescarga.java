package cl.licitawatch.licitaciones.bs.dto.response;

public record ArchivoDescarga(byte[] contenido, String contentType, String nombre, boolean inline) {
}
