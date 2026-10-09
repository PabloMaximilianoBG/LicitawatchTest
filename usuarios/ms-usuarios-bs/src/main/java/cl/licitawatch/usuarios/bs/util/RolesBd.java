package cl.licitawatch.usuarios.bs.util;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.seguridad.Roles;

/** Traducción entre el código de rol del JWT (LICITADOR) y el nombre del catálogo rol (Licitador). */
public final class RolesBd {
    private RolesBd() {
    }

    public static String aCodigo(String nombreBd) {
        if (nombreBd == null) {
            return null;
        }
        return switch (nombreBd.toLowerCase()) {
            case "licitador" -> Roles.LICITADOR;
            case "pyme" -> Roles.PYME;
            case "administrador" -> Roles.ADMINISTRADOR;
            default -> nombreBd.toUpperCase();
        };
    }

    public static String aNombreBd(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return switch (codigo.toUpperCase()) {
            case Roles.LICITADOR -> "Licitador";
            case Roles.PYME -> "Pyme";
            case Roles.ADMINISTRADOR -> "Administrador";
            default -> throw new BadRequestException("ROL_INVALIDO", "Rol inválido: usa LICITADOR, PYME o ADMINISTRADOR");
        };
    }
}
