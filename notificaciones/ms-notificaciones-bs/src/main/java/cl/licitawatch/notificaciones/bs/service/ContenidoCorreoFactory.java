package cl.licitawatch.notificaciones.bs.service;

import cl.licitawatch.notificaciones.bs.service.model.CorreoContenido;

import java.util.Map;

/** Arma el contenido del correo de cada tipo de notificación del ER y de los correos de cuenta. */
public interface ContenidoCorreoFactory {
    CorreoContenido evento(String tipo, String nombre, Map<String, String> datos);

    CorreoContenido confirmacionCuenta(String nombre, String enlace);

    CorreoContenido restablecerPassword(String nombre, String enlace);

    CorreoContenido soporte(String nombre, String email, String rol, boolean prioritario, String asunto, String mensaje);

    CorreoContenido acuseSoporte(String nombre, boolean prioritario, String asunto);
}
