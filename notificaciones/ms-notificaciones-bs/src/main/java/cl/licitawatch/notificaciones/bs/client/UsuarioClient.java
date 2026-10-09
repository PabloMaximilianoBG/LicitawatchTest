package cl.licitawatch.notificaciones.bs.client;

import cl.licitawatch.notificaciones.bs.client.dto.UsuarioDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/** REF notificacion.usuario_id: se valida y se obtiene el correo en MS.usuarios.bs. */
@HttpExchange("/bs/usuarios")
public interface UsuarioClient {
    @GetExchange("/{id}")
    UsuarioDto usuario(@PathVariable Integer id);
}
