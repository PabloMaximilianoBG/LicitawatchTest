package cl.licitawatch.usuarios.bs.client;

import cl.licitawatch.usuarios.bs.client.dto.UsuarioIdDto;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/** Contrato REST de MS.ventas.bs usado por usuarios (plan Estándar por defecto e insignia Premium). */
@HttpExchange("/bs/suscripciones")
public interface VentasClient {

    @PostExchange("/estandar")
    void asignarEstandar(@RequestBody UsuarioIdDto request);

    @GetExchange("/premium")
    List<Integer> usuariosPremium(@RequestParam List<Integer> usuarioIds);
}
