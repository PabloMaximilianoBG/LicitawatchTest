package cl.licitawatch.chat.bs.client;

import cl.licitawatch.chat.bs.client.dto.ContextoPostulacionDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** REF conversacion.postulacion_id: se valida contra MS.licitaciones.bs. */
@HttpExchange("/bs/postulaciones")
public interface LicitacionClient {
    @GetExchange("/{id}/contexto")
    ContextoPostulacionDto contexto(@PathVariable Integer id);
}
