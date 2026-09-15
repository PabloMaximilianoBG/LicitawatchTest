package cl.licitawatch.usuarios.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiraEnSegundos,
        UsuarioResumen usuario
) {
}
