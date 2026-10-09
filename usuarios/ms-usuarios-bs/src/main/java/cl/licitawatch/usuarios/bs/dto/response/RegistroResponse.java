package cl.licitawatch.usuarios.bs.dto.response;

public record RegistroResponse(PerfilResponse usuario, boolean correoEnviado, String mensaje) {
}
