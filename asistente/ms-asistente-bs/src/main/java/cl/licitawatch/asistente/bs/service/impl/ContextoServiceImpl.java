package cl.licitawatch.asistente.bs.service.impl;

import cl.licitawatch.asistente.bs.client.LicitacionesClient;
import cl.licitawatch.asistente.bs.client.UsuariosClient;
import cl.licitawatch.asistente.bs.client.VentasClient;
import cl.licitawatch.asistente.bs.service.ContextoService;
import cl.licitawatch.common.seguridad.UsuarioActual;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Solo se envían a Groq campos de una lista blanca: nunca contraseñas, JWT, correos, teléfonos ni identificadores de pago.
 * Las consultas viajan con el usuario actual, por lo que cada API aplica su RBAC (no se filtran datos de terceros).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContextoServiceImpl implements ContextoService {
    static final String[] LICITACION = {"id", "titulo", "descripcion", "rubroNombre", "regionNombre", "presupuestoMin", "presupuestoMax",
            "maxPostulantes", "cuposDisponibles", "fechaCierre", "diasParaCierre", "estado", "licitadorNombre", "cantidadPostulaciones",
            "miPostulacionEstado", "archivoNombre"};
    static final String[] LICITACION_BREVE = {"id", "titulo", "rubroNombre", "regionNombre", "presupuestoMin", "presupuestoMax",
            "fechaCierre", "diasParaCierre", "estado", "licitadorNombre", "cantidadPostulaciones", "miPostulacionEstado"};
    static final String[] POSTULACION_PYME = {"id", "licitacionId", "licitacionTitulo", "licitacionEstado", "fechaCierre",
            "licitadorNombre", "fechaPostulacion", "estado", "mensaje"};
    static final String[] POSTULANTE = {"id", "pymeRazonSocial", "pymeRubro", "pymeCiudad", "pymeRegion", "pymeTamano", "pymePremium",
            "fechaPostulacion", "estado", "mensaje"};
    static final String[] USUARIO_ADMIN = {"usuarioId", "rol", "estadoCuenta", "razonSocial", "nombre", "rubroNombre", "regionNombre",
            "premium", "createdAt"};
    static final String[] POSTULACION_ADMIN = {"id", "licitacionTitulo", "pymeRazonSocial", "licitadorNombre", "estado", "fechaPostulacion"};
    static final String[] VENTA_ADMIN = {"id", "clienteNombre", "plan", "monto", "fecha", "estadoPago", "metodoPago"};
    static final String[] SUSCRIPCION_ADMIN = {"id", "clienteNombre", "plan", "estadoVisible", "fechaInicio", "fechaVencimiento"};

    private final UsuariosClient usuarios;
    private final LicitacionesClient licitaciones;
    private final VentasClient ventas;
    private final ObjectMapper om;

    @Override
    public Contexto construir(UsuarioActual u, Integer licitacionId) {
        ObjectNode ctx = om.createObjectNode();
        List<String> fuentes = new ArrayList<>();
        ctx.put("rol", u.rol());
        consultar(usuarios::me).ifPresent(me -> {
            ctx.put("usuario", me.path("razonSocial").isMissingNode() || me.path("razonSocial").isNull()
                    ? me.path("nombre").asText() : me.path("razonSocial").asText());
            if (!me.path("rubroNombre").isNull()) {
                ctx.put("miRubro", me.path("rubroNombre").asText());
            }
        });
        switch (u.rol()) {
            case "PYME" -> pyme(ctx, fuentes);
            case "LICITADOR" -> licitador(ctx, fuentes);
            default -> admin(ctx, fuentes);
        }
        if (licitacionId != null) {
            consultar(() -> licitaciones.licitacion(licitacionId)).ifPresent(l -> {
                ctx.set("licitacionConsultada", filtrar(l, LICITACION));
                fuentes.add("Licitación #" + licitacionId);
                if (!u.esPyme()) {
                    consultar(() -> licitaciones.postulantes(licitacionId))
                            .ifPresent(p -> ctx.set("postulantesLicitacionConsultada", filtrarLista(p, POSTULANTE, 30)));
                }
            });
        }
        return new Contexto(ctx.toString(), fuentes);
    }

    private void pyme(ObjectNode ctx, List<String> fuentes) {
        consultar(licitaciones::uso).ifPresent(uso -> ctx.set("usoDelPlan", uso));
        consultar(() -> licitaciones.buscar("cierre", 0, 40)).ifPresent(p -> {
            ctx.set("licitacionesAbiertas", filtrarLista(p.path("content"), LICITACION, 40));
            ctx.put("totalLicitacionesAbiertas", p.path("totalElements").asLong());
            fuentes.add("Licitaciones abiertas (" + p.path("totalElements").asLong() + ")");
        });
        consultar(licitaciones::misPostulaciones).ifPresent(p -> {
            ctx.set("misPostulaciones", filtrarLista(p, POSTULACION_PYME, 30));
            fuentes.add("Mis postulaciones (" + p.size() + ")");
        });
    }

    private void licitador(ObjectNode ctx, List<String> fuentes) {
        consultar(licitaciones::mias).ifPresent(lista -> {
            ArrayNode mias = om.createArrayNode();
            int conPostulantes = 0;
            for (JsonNode l : lista) {
                if (mias.size() >= 25) {
                    break;
                }
                ObjectNode item = filtrar(l, LICITACION);
                if (l.path("cantidadPostulaciones").asLong() > 0 && conPostulantes < 5) {
                    consultar(() -> licitaciones.postulantes(l.path("id").asInt()))
                            .ifPresent(p -> item.set("postulantes", filtrarLista(p, POSTULANTE, 20)));
                    conPostulantes++;
                }
                mias.add(item);
            }
            ctx.set("misLicitaciones", mias);
            fuentes.add("Mis licitaciones (" + lista.size() + ")");
        });
        consultar(() -> licitaciones.buscar("recientes", 0, 15)).ifPresent(p -> {
            ctx.set("licitacionesAbiertasEnLaPlataforma", filtrarLista(p.path("content"), LICITACION_BREVE, 15));
            fuentes.add("Licitaciones abiertas recientes");
        });
    }

    private void admin(ObjectNode ctx, List<String> fuentes) {
        consultar(ventas::resumen).ifPresent(r -> {
            ctx.set("resumenVentas", r);
            fuentes.add("Resumen de ventas");
        });
        consultar(() -> usuarios.adminUsuarios(0, 30)).ifPresent(p -> {
            ctx.set("usuariosRecientes", filtrarLista(p.path("content"), USUARIO_ADMIN, 30));
            ctx.put("totalUsuarios", p.path("totalElements").asLong());
            fuentes.add("Usuarios (" + p.path("totalElements").asLong() + ")");
        });
        consultar(() -> licitaciones.adminLicitaciones(0, 30)).ifPresent(p -> {
            ctx.set("licitacionesRecientes", filtrarLista(p.path("content"), LICITACION_BREVE, 30));
            ctx.put("totalLicitaciones", p.path("totalElements").asLong());
            fuentes.add("Licitaciones (" + p.path("totalElements").asLong() + ")");
        });
        consultar(() -> licitaciones.adminPostulaciones(0, 30))
                .ifPresent(p -> ctx.set("postulacionesRecientes", filtrarLista(p.path("content"), POSTULACION_ADMIN, 30)));
        consultar(() -> ventas.adminVentas(0, 20)).ifPresent(p -> ctx.set("ventasRecientes", filtrarLista(p.path("content"), VENTA_ADMIN, 20)));
        consultar(() -> ventas.adminSuscripciones(0, 20))
                .ifPresent(p -> ctx.set("suscripcionesRecientes", filtrarLista(p.path("content"), SUSCRIPCION_ADMIN, 20)));
        fuentes.add("Listados administrativos recientes");
    }

    private java.util.Optional<JsonNode> consultar(Supplier<JsonNode> s) {
        try {
            return java.util.Optional.ofNullable(s.get());
        } catch (Exception e) {
            log.warn("Dato de contexto no disponible: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

    ObjectNode filtrar(JsonNode n, String[] campos) {
        ObjectNode o = om.createObjectNode();
        for (String c : campos) {
            JsonNode v = n.get(c);
            if (v != null && !v.isNull()) {
                o.set(c, v);
            }
        }
        return o;
    }

    ArrayNode filtrarLista(JsonNode lista, String[] campos, int max) {
        ArrayNode a = om.createArrayNode();
        if (lista != null && lista.isArray()) {
            for (JsonNode n : lista) {
                if (a.size() >= max) {
                    break;
                }
                a.add(filtrar(n, campos));
            }
        }
        return a;
    }
}
