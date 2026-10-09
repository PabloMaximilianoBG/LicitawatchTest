package cl.licitawatch.chat.bd.service;

import cl.licitawatch.chat.bd.dto.request.ConversacionBdRequest;
import cl.licitawatch.chat.bd.dto.request.MensajeBdRequest;
import cl.licitawatch.chat.bd.dto.response.ConversacionBdResponse;
import cl.licitawatch.chat.bd.dto.response.MensajeBdResponse;

import java.util.List;

public interface ChatDataService {
    ConversacionBdResponse crear(ConversacionBdRequest request);

    ConversacionBdResponse obtener(Integer id, Integer lectorId);

    ConversacionBdResponse porPostulacion(Integer postulacionId, Integer lectorId);

    List<ConversacionBdResponse> listar(Integer licitadorId, Integer pymeId, Integer lectorId);

    int eliminarPorPostulaciones(List<Integer> postulacionIds);

    List<MensajeBdResponse> mensajes(Integer conversacionId, Integer despuesDeId);

    MensajeBdResponse enviar(Integer conversacionId, MensajeBdRequest request);

    int marcarLeidos(Integer conversacionId, Integer lectorId);

    long noLeidos(Integer conversacionId, Integer lectorId);
}
