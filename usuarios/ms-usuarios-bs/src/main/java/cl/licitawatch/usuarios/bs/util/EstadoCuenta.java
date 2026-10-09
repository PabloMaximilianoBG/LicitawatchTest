package cl.licitawatch.usuarios.bs.util;

/**
 * Estado de la cuenta derivado SOLO de columnas del ER (usuario.activo y usuario.password):
 *  - ACTIVA: activo = true.
 *  - DESACTIVADA: activo = false y el hash está bloqueado con el prefijo "!" (convención de cuentas
 *    bloqueadas de Unix/shadow), lo que hace el Administrador al desactivar.
 *  - PENDIENTE_CONFIRMACION: activo = false sin bloqueo (recién registrada, falta confirmar el correo).
 */
public enum EstadoCuenta {
    ACTIVA, PENDIENTE_CONFIRMACION, DESACTIVADA;

    public static final String PREFIJO_BLOQUEO = "!";

    public static EstadoCuenta de(Boolean activo, String passwordHash) {
        if (Boolean.TRUE.equals(activo)) {
            return ACTIVA;
        }
        return passwordHash != null && passwordHash.startsWith(PREFIJO_BLOQUEO) ? DESACTIVADA : PENDIENTE_CONFIRMACION;
    }

    public static String bloquear(String hash) {
        return hash.startsWith(PREFIJO_BLOQUEO) ? hash : PREFIJO_BLOQUEO + hash;
    }

    public static String desbloquear(String hash) {
        return hash.startsWith(PREFIJO_BLOQUEO) ? hash.substring(1) : hash;
    }
}
