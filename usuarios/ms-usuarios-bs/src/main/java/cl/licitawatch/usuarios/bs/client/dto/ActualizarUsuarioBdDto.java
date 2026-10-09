package cl.licitawatch.usuarios.bs.client.dto;

import lombok.Builder;

@Builder
public record ActualizarUsuarioBdDto(Boolean activo, String password, String rolNombre) {
}
