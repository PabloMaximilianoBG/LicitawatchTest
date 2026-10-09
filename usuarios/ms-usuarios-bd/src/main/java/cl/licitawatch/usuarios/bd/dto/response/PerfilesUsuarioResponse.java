package cl.licitawatch.usuarios.bd.dto.response;

/** Ids de los perfiles que tiene un usuario (null si no existe). */
public record PerfilesUsuarioResponse(Integer licitadorId, Integer pymeId, Integer administradorId) {
}
