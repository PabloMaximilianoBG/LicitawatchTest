package cl.licitawatch.common.seguridad;

import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.UnauthorizedException;

import java.util.Arrays;
import java.util.Optional;

/** Usuario de la petición en curso (ThreadLocal poblado por {@link InternalAuthFilter}). */
public final class ContextoUsuario {
    private static final ThreadLocal<UsuarioActual> ACTUAL = new ThreadLocal<>();

    private ContextoUsuario() {
    }

    public static void establecer(UsuarioActual u) {
        ACTUAL.set(u);
    }

    public static void limpiar() {
        ACTUAL.remove();
    }

    public static Optional<UsuarioActual> actual() {
        return Optional.ofNullable(ACTUAL.get());
    }

    public static UsuarioActual requerido() {
        return actual().orElseThrow(() -> new UnauthorizedException("Debes iniciar sesión"));
    }

    /** Exige que el usuario tenga alguno de los roles indicados (403 si no). */
    public static UsuarioActual exigirRol(String... roles) {
        UsuarioActual u = requerido();
        if (Arrays.stream(roles).noneMatch(r -> r.equals(u.rol()))) {
            throw new ForbiddenException("No tienes permisos para realizar esta acción");
        }
        return u;
    }
}
