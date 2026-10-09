package cl.licitawatch.usuarios.bd.dto.response;

import java.time.LocalDateTime;

/** Fila de usuario (incluye el hash: este servicio solo es accesible por MS.usuarios.bs). */
public record UsuarioBdResponse(Integer id, String email, String password, String rolNombre, Boolean activo,
                                LocalDateTime createdAt) {
}
