package cl.licitawatch.common.seguridad;

/** Cabeceras que el api-gateway agrega tras validar el JWT y que se propagan BFF -> BS -> BD. */
public final class CabecerasInternas {
    public static final String INTERNAL_KEY = "X-Internal-Key";
    public static final String USUARIO_ID = "X-User-Id";
    public static final String USUARIO_EMAIL = "X-User-Email";
    public static final String USUARIO_ROL = "X-User-Rol";
    public static final String PERFIL_ID = "X-Perfil-Id";

    private CabecerasInternas() {
    }
}
