package cl.licitawatch.common.seguridad;

/** Usuario autenticado (datos del JWT validado por el api-gateway). perfilId = id en licitador / pyme / administrador. */
public record UsuarioActual(Integer usuarioId, String email, String rol, Integer perfilId) {

    public boolean esPyme() {
        return Roles.PYME.equals(rol);
    }

    public boolean esLicitador() {
        return Roles.LICITADOR.equals(rol);
    }

    public boolean esAdministrador() {
        return Roles.ADMINISTRADOR.equals(rol);
    }
}
