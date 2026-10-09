package cl.licitawatch.usuarios.bs.dto.response;

import java.time.OffsetDateTime;

public record LoginResponse(String token, String tipo, OffsetDateTime expiraEn, PerfilResponse usuario) {
}
