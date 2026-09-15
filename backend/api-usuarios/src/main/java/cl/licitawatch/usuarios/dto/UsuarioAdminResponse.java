package cl.licitawatch.usuarios.dto;

import java.time.LocalDateTime;

public record UsuarioAdminResponse(
        Long id,
        String email,
        String rol,
        boolean activo,
        LocalDateTime creadoEn
) {
}
