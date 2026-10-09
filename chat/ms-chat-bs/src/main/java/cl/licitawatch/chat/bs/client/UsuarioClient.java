package cl.licitawatch.chat.bs.client;

import cl.licitawatch.chat.bs.client.dto.PerfilDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** REF licitador_id / pyme_id: nombres y usuario de cada parte. */
@HttpExchange("/bs")
public interface UsuarioClient {
    @GetExchange("/licitadores/{id}")
    PerfilDto licitador(@PathVariable Integer id);

    @GetExchange("/pymes/{id}")
    PerfilDto pyme(@PathVariable Integer id);
}
