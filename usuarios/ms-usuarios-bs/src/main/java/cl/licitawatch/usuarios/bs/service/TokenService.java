package cl.licitawatch.usuarios.bs.service;

import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;

import java.time.OffsetDateTime;

/** Emisión y verificación de tokens firmados (JWT de acceso y tokens de un propósito para correos). */
public interface TokenService {

    record TokenAcceso(String token, OffsetDateTime expiraEn) {
    }

    TokenAcceso emitirAcceso(PerfilResponse perfil);

    String emitirConfirmacionCuenta(Integer usuarioId);

    String emitirRestablecerPassword(Integer usuarioId, String passwordHashActual);

    /** Devuelve el usuarioId si el token de confirmación es válido. */
    Integer verificarConfirmacionCuenta(String token);

    /** Devuelve el usuarioId si el token es válido y la contraseña no cambió desde que se emitió. */
    Integer verificarRestablecerPassword(String token, java.util.function.IntFunction<String> hashActualDe);
}
