package cl.licitawatch.asistente.bs.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange("/bs")
public interface UsuariosClient {
    @GetExchange("/usuarios/me")
    JsonNode me();

    @GetExchange("/admin/usuarios")
    JsonNode adminUsuarios(@RequestParam int page, @RequestParam int size);
}
