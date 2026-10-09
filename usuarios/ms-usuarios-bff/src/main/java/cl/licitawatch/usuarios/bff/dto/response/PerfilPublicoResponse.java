package cl.licitawatch.usuarios.bff.dto.response;

/** Vista de la empresa que ve la contraparte (Licitador ve a la Pyme postulante y viceversa). Sin datos de la cuenta. */
public record PerfilPublicoResponse(Integer perfilId, String rol, String razonSocial, String rut, String nombreContacto,
                                    String emailContacto, String telefono, String rubroNombre, String ciudadNombre,
                                    String regionNombre, String tamanoEmpresaNombre, String descripcionEmpresa,
                                    String sitioWeb, boolean premium) {

    public static PerfilPublicoResponse de(PerfilResponse p) {
        return new PerfilPublicoResponse(p.perfilId(), p.rol(), p.razonSocial(), p.rut(), p.nombreContacto(), p.emailContacto(),
                p.telefono(), p.rubroNombre(), p.ciudadNombre(), p.regionNombre(), p.tamanoEmpresaNombre(),
                p.descripcionEmpresa(), p.sitioWeb(), p.premium());
    }
}
