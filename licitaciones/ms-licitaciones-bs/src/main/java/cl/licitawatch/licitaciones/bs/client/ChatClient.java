package cl.licitawatch.licitaciones.bs.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

/** Contrato REST de MS.chat.bs: al eliminar una licitación se borran sus conversaciones. */
@HttpExchange("/bs/conversaciones")
public interface ChatClient {
    @DeleteExchange
    void eliminarPorPostulaciones(@RequestParam List<Integer> postulacionIds);
}
