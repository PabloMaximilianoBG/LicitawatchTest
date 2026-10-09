package cl.licitawatch.usuarios.bff.dto.response;

import java.time.OffsetDateTime;

public record LoginResponse(String token, String tipo, OffsetDateTime expiraEn, PerfilResponse usuario) {
}
