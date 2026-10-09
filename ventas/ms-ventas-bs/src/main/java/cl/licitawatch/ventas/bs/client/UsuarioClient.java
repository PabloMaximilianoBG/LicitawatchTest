package cl.licitawatch.ventas.bs.client;

import cl.licitawatch.ventas.bs.client.dto.UsuarioDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** Contrato REST de MS.usuarios.bs: valida la REF suscripcion.usuario_id y obtiene nombre/correo. */
@HttpExchange("/bs/usuarios")
public interface UsuarioClient {
    @GetExchange("/{id}")
    UsuarioDto usuario(@PathVariable Integer id);
}
