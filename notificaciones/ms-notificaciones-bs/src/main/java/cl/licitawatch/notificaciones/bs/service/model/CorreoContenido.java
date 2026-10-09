package cl.licitawatch.notificaciones.bs.service.model;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/** Modelo de la plantilla templates/email/base.html. */
@Builder
public record CorreoContenido(String asunto, String etiqueta, String preheader, String titulo, String saludo, List<String> parrafos,
                              String detallesTitulo, List<Map.Entry<String, String>> detalles, String montoDestacado,
                              String botonTexto, String botonUrl, boolean mostrarEnlace, String aviso, String estadoTexto,
                              String estadoColor, String estadoFondo) {
}
