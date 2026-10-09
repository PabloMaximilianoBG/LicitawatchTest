package cl.licitawatch.ventas.bs.service;

import cl.licitawatch.ventas.bs.client.dto.UsuarioDto;

import java.util.List;
import java.util.Map;

/** Nombre y correo de los clientes (REF suscripcion.usuario_id) consultados a MS.usuarios.bs. */
public interface ClienteService {
    Map<Integer, UsuarioDto> clientes(List<Integer> usuarioIds);
}
