package cl.licitawatch.usuarios.bff.dto.response;

public record RegistroResponse(PerfilResponse usuario, boolean correoEnviado, String mensaje) {
}
