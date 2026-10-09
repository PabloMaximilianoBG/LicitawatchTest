package cl.licitawatch.usuarios.bd.dto.request;

import lombok.Builder;

/** Campos opcionales de la tabla usuario (null = no cambia). */
@Builder
public record ActualizarUsuarioBdRequest(Boolean activo, String password, String rolNombre) {
}
