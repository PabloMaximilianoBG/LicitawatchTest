package cl.licitawatch.usuarios.bs.client.dto;

import java.time.LocalDateTime;

public record UsuarioBdDto(Integer id, String email, String password, String rolNombre, Boolean activo, LocalDateTime createdAt) {
}
