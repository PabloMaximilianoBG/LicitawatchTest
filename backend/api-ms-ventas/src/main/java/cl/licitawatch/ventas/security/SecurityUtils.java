package cl.licitawatch.ventas.security;

import cl.licitawatch.ventas.exception.TokenInvalidoException;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser usuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                : null;
        if (!(principal instanceof AuthenticatedUser usuario)) {
            throw new TokenInvalidoException("No hay una sesion autenticada valida");
        }
        return usuario;
    }
}
