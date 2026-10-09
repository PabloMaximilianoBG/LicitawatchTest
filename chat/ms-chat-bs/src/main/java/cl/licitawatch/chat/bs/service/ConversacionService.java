package cl.licitawatch.chat.bs.service;

import cl.licitawatch.chat.bs.dto.request.MensajeRequest;
import cl.licitawatch.chat.bs.dto.response.ConversacionResponse;
import cl.licitawatch.chat.bs.dto.response.MensajeResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;

import java.util.List;

/**
 * PPT diap. 7-8: chat privado Licitador-Pyme cuando el Licitador adjudica la licitación.
 * Decisión del cliente: queda disponible al aprobar la postulación y el Licitador la abre con "Chatear".
 */
public interface ConversacionService {
    ConversacionResponse abrir(UsuarioActual licitador, Integer postulacionId);

    ConversacionResponse porPostulacion(UsuarioActual usuario, Integer postulacionId);

    ConversacionResponse obtener(UsuarioActual usuario, Integer conversacionId);

    List<ConversacionResponse> mias(UsuarioActual usuario);

    List<MensajeResponse> mensajes(UsuarioActual usuario, Integer conversacionId, Integer despuesDeId);

    MensajeResponse enviar(UsuarioActual usuario, Integer conversacionId, MensajeRequest request);

    int eliminarPorPostulaciones(List<Integer> postulacionIds);
}
