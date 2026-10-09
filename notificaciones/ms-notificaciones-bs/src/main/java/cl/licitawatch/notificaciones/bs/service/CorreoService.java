package cl.licitawatch.notificaciones.bs.service;

import cl.licitawatch.notificaciones.bs.service.model.CorreoContenido;

/** Envío de correos por Gmail SMTP (servicio externo servicio-gmail-smtp, PPT diap. 12). */
public interface CorreoService {
    void enviar(String destinatario, CorreoContenido contenido);

    void enviarSoporte(String destinatario, String responderA, boolean prioritario, CorreoContenido contenido);
}
