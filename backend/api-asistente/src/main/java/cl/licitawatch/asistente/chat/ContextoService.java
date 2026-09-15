package cl.licitawatch.asistente.chat;

import cl.licitawatch.asistente.contexto.LicitacionContextoClient;
import cl.licitawatch.asistente.contexto.LicitacionResponse;
import cl.licitawatch.asistente.contexto.PlanCatalogoResponse;
import cl.licitawatch.asistente.contexto.PostulacionResponse;
import cl.licitawatch.asistente.contexto.SuscripcionResponse;
import cl.licitawatch.asistente.contexto.UsuarioContextoClient;
import cl.licitawatch.asistente.contexto.UsuarioInternoResponse;
import cl.licitawatch.asistente.contexto.VentaContextoClient;
import cl.licitawatch.asistente.security.AuthenticatedUser;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Arma el bloque de contexto real (seccion 3.5, paso 4-5) que se inserta en
 * el prompt del sistema antes de llamar a Groq. Cada dato faltante se marca
 * explicitamente como "no disponible" en vez de omitirse en silencio, para
 * que el modelo nunca tenga que adivinar/inventar una cifra que no le llego.
 *
 * A proposito NO se intenta clasificar la "intencion" del mensaje del
 * usuario con un motor de reglas: se junta un contexto razonable segun el
 * rol (licitaciones/postulaciones propias, plan vigente, y para Clientes
 * ademas licitaciones publicadas) y se deja que el propio modelo de lenguaje
 * decida que parte usar para responder la pregunta especifica - es el
 * patron estandar para este tipo de asistente "RAG ligero", y evita
 * construir un clasificador de intenciones fragil que de todos modos Groq
 * puede resolver mejor con el contexto correcto a la vista.
 */
@Service
public class ContextoService {

    private static final int MAX_LICITACIONES_EN_CONTEXTO = 15;

    private final UsuarioContextoClient usuarioContextoClient;
    private final VentaContextoClient ventaContextoClient;
    private final LicitacionContextoClient licitacionContextoClient;

    public ContextoService(
            UsuarioContextoClient usuarioContextoClient,
            VentaContextoClient ventaContextoClient,
            LicitacionContextoClient licitacionContextoClient) {
        this.usuarioContextoClient = usuarioContextoClient;
        this.ventaContextoClient = ventaContextoClient;
        this.licitacionContextoClient = licitacionContextoClient;
    }

    public String construir(AuthenticatedUser usuario, String authorizationHeader) {
        StringBuilder sb = new StringBuilder();

        Optional<UsuarioInternoResponse> perfil = usuarioContextoClient.resolver(usuario.id());
        sb.append("Perfil: rol=").append(usuario.rol())
                .append(", nombre=").append(perfil.map(UsuarioInternoResponse::nombre).orElse("no disponible"))
                .append(".\n");

        Optional<SuscripcionResponse> suscripcion = ventaContextoClient.obtenerSuscripcion(usuario.id());
        if (suscripcion.isPresent() && suscripcion.get().plan() != null) {
            SuscripcionResponse s = suscripcion.get();
            sb.append("Plan de suscripcion: ").append(s.plan())
                    .append(", estado=").append(s.estado())
                    .append(", vence=").append(s.fechaVencimiento() != null ? s.fechaVencimiento() : "no aplica")
                    .append(".\n");
        } else {
            sb.append("Plan de suscripcion: no tiene una suscripcion registrada.\n");
        }

        List<PlanCatalogoResponse> planes = ventaContextoClient.obtenerCatalogoPlanes(authorizationHeader);
        sb.append(formatearCatalogoPlanes(planes));

        if ("EMPRESA".equals(usuario.rol())) {
            List<LicitacionResponse> propias = licitacionContextoClient.misLicitaciones(authorizationHeader);
            sb.append(formatearLicitaciones("Licitaciones publicadas por esta empresa", propias));
        } else if ("CLIENTE".equals(usuario.rol())) {
            List<PostulacionResponse> postulaciones = licitacionContextoClient.misPostulaciones(authorizationHeader);
            sb.append(formatearPostulaciones(postulaciones));

            List<LicitacionResponse> publicadas = licitacionContextoClient.licitacionesPublicadas(authorizationHeader);
            sb.append(formatearLicitaciones("Licitaciones actualmente publicadas en la plataforma (puede no ser la lista completa)",
                    publicadas.stream().limit(MAX_LICITACIONES_EN_CONTEXTO).toList()));
        }

        return sb.toString();
    }

    private String formatearLicitaciones(String titulo, List<LicitacionResponse> licitaciones) {
        if (licitaciones.isEmpty()) {
            return titulo + ": no hay datos disponibles en este momento.\n";
        }
        StringBuilder sb = new StringBuilder(titulo).append(":\n");
        for (LicitacionResponse l : licitaciones) {
            sb.append("- [id ").append(l.id()).append("] \"").append(l.titulo()).append("\" (rubro: ")
                    .append(l.rubro()).append(", region: ").append(l.region()).append(", estado: ").append(l.estado())
                    .append(", cierra: ").append(l.fechaCierre()).append(")\n");
        }
        return sb.toString();
    }

    private String formatearCatalogoPlanes(List<PlanCatalogoResponse> planes) {
        if (planes.isEmpty()) {
            return "Catalogo de planes: no disponible en este momento.\n";
        }
        StringBuilder sb = new StringBuilder("Catalogo de planes de suscripcion:\n");
        for (PlanCatalogoResponse p : planes) {
            sb.append("- ").append(p.nombre()).append(": $").append(p.precio())
                    .append(p.precio().signum() == 0 ? " (gratuito, incluido por defecto)" : " /mes")
                    .append(", publicaciones/mes: ").append(p.limitePublicacionesMes() == null ? "ilimitadas" : p.limitePublicacionesMes())
                    .append(", postulaciones/mes: ").append(p.limitePostulacionesMes() == null ? "ilimitadas" : p.limitePostulacionesMes())
                    .append(", soporte prioritario: ").append(p.soportePrioritario() ? "si" : "no")
                    .append(", notificaciones automaticas: ").append(p.notificacionesAutomaticas() ? "si" : "no")
                    .append("\n");
        }
        return sb.toString();
    }

    private String formatearPostulaciones(List<PostulacionResponse> postulaciones) {
        if (postulaciones.isEmpty()) {
            return "Postulaciones de este cliente: no hay datos disponibles en este momento.\n";
        }
        StringBuilder sb = new StringBuilder("Postulaciones de este cliente:\n");
        for (PostulacionResponse p : postulaciones) {
            sb.append("- Licitacion \"").append(p.licitacionTitulo()).append("\": estado=").append(p.estado())
                    .append(", postulada el ").append(p.fechaPostulacion()).append("\n");
        }
        return sb.toString();
    }
}
