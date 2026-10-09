package cl.licitawatch.licitaciones.bs.service;

import cl.licitawatch.licitaciones.bs.client.dto.LicitacionBdDto;

import java.util.Map;

/** Envío asíncrono de eventos a MS.notificaciones.bs (un fallo de correo nunca rompe el flujo de negocio). */
public interface NotificadorService {
    void notificar(Integer usuarioId, String tipo, Map<String, String> datos);

    /** PPT diap. 7 (Premium): alertas por correo de licitaciones del rubro de la Pyme. */
    void alertarPymesPremiumDelRubro(LicitacionBdDto licitacion, String rubroNombre, String regionNombre);
}
