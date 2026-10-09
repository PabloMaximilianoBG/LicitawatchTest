package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.client.LicitacionBdClient;
import cl.licitawatch.licitaciones.bs.client.UsuarioClient;
import cl.licitawatch.licitaciones.bs.client.VentasClient;
import cl.licitawatch.licitaciones.bs.client.dto.*;
import cl.licitawatch.licitaciones.bs.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bs.dto.response.ContextoPostulacionResponse;
import cl.licitawatch.licitaciones.bs.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bs.dto.response.UsoPlanResponse;
import cl.licitawatch.licitaciones.bs.mapper.LicitacionMapper;
import cl.licitawatch.licitaciones.bs.service.NotificadorService;
import cl.licitawatch.licitaciones.bs.service.PostulacionService;
import cl.licitawatch.licitaciones.bs.util.Estados;
import cl.licitawatch.licitaciones.bs.util.Fechas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostulacionServiceImpl implements PostulacionService {
    private final LicitacionBdClient bd;
    private final UsuarioClient usuarios;
    private final VentasClient ventas;
    private final NotificadorService notificador;
    private final LicitacionMapper mapper;

    @Override
    public PostulacionResponse postular(UsuarioActual u, Integer licitacionId, PostularRequest r) {
        if (!u.esPyme()) {
            throw new ForbiddenException("Solo las Pymes pueden postular");
        }
        LicitacionBdDto l = bd.obtener(licitacionId);
        if (!Estados.ABIERTA.equals(l.estado()) || l.fechaCierre().isBefore(Fechas.hoy())) {
            throw new BusinessException("LICITACION_CERRADA", "La licitación ya no recibe postulaciones");
        }
        if (Boolean.TRUE.equals(bd.existePostulacion(licitacionId, u.perfilId()))) {
            throw new ConflictException("POSTULACION_DUPLICADA", "Ya postulaste a esta licitación");
        }
        if (l.maxPostulantes() != null && l.cantidadPostulaciones() >= l.maxPostulantes()) {
            throw new BusinessException("SIN_CUPOS", "La licitación alcanzó el máximo de postulantes");
        }
        UsoPlanResponse uso = usoPlan(u);
        if (uso.limitePostulacionesMes() != null && uso.restantes() <= 0) {
            throw new ForbiddenException("LIMITE_PLAN", "Alcanzaste el límite de " + uso.limitePostulacionesMes()
                    + " postulaciones de este mes del plan " + uso.plan()
                    + (uso.premium() ? "." : ". Con Premium puedes postular hasta 7 veces al mes."));
        }
        PerfilDto pyme = perfilPyme(u.perfilId());
        PostulacionBdDto creada = bd.crearPostulacion(new PostulacionBdRequestDto(licitacionId, u.perfilId(), r.mensaje()));
        try {
            PerfilDto licitador = usuarios.licitador(l.licitadorId());
            notificador.notificar(licitador.usuarioId(), Estados.NOTIF_POSTULACION_RECIBIDA, Map.of(
                    "licitacionId", String.valueOf(l.id()), "titulo", l.titulo(), "razonSocial", pyme.razonSocial(),
                    "fechaCierre", String.valueOf(l.fechaCierre())));
        } catch (Exception e) {
            log.warn("No se pudo notificar la postulación {}: {}", creada.id(), e.getMessage());
        }
        return mapper.postulacion(creada, pyme, null);
    }

    @Override
    public List<PostulacionResponse> misPostulaciones(UsuarioActual u) {
        if (!u.esPyme()) {
            throw new ForbiddenException("Solo las Pymes tienen postulaciones");
        }
        List<PostulacionBdDto> lista = bd.postulaciones(null, u.perfilId(), null, 0, 200).content();
        Map<Integer, String> licitadores = nombresLicitadores(lista);
        return lista.stream().map(p -> mapper.postulacion(p, null, licitadores.get(p.licitadorId()))).toList();
    }

    /** Postulantes de una licitación: las Pymes Premium aparecen primero (prioridad de visibilidad, PPT diap. 7). */
    @Override
    public List<PostulacionResponse> postulantes(UsuarioActual u, Integer licitacionId) {
        LicitacionBdDto l = bd.obtener(licitacionId);
        LicitacionServiceImpl.exigirDuenoOAdmin(u, l);
        List<PostulacionBdDto> lista = bd.postulacionesDeLicitacion(licitacionId);
        Map<Integer, PerfilDto> pymes = perfilesPymes(lista);
        return lista.stream().map(p -> mapper.postulacion(p, pymes.get(p.pymeId()), null))
                .sorted(Comparator.comparing(PostulacionResponse::pymePremium).reversed()
                        .thenComparing(PostulacionResponse::fechaPostulacion).thenComparing(PostulacionResponse::id))
                .toList();
    }

    @Override
    public PostulacionResponse aprobar(UsuarioActual u, Integer postulacionId) {
        PostulacionBdDto p = bd.postulacion(postulacionId);
        LicitacionBdDto l = bd.obtener(p.licitacionId());
        LicitacionServiceImpl.exigirDuenoOAdmin(u, l);
        if (Estados.ADJUDICADA.equals(l.estado())) {
            throw new BusinessException("LICITACION_ADJUDICADA", "La licitación ya fue adjudicada");
        }
        if (!Estados.PENDIENTE.equals(p.estado())) {
            throw new BusinessException("POSTULACION_NO_PENDIENTE", "Solo se puede aprobar una postulación pendiente");
        }
        AdjudicacionBdDto resultado = bd.adjudicar(l.id(), new AdjudicarDto(postulacionId));
        List<PostulacionBdDto> afectadas = new ArrayList<>(resultado.rechazadas());
        afectadas.add(resultado.aprobada());
        Map<Integer, PerfilDto> pymes = perfilesPymes(afectadas);
        PerfilDto ganadora = pymes.get(resultado.aprobada().pymeId());
        if (ganadora != null) {
            notificador.notificar(ganadora.usuarioId(), Estados.NOTIF_POSTULACION_APROBADA, Map.of(
                    "licitacionId", String.valueOf(l.id()), "titulo", l.titulo(), "razonSocial", ganadora.razonSocial()));
        }
        // Decisión del cliente: las demás pymes son notificadas de que la licitación ya fue adjudicada.
        for (PostulacionBdDto rechazada : resultado.rechazadas()) {
            PerfilDto pyme = pymes.get(rechazada.pymeId());
            if (pyme != null) {
                notificador.notificar(pyme.usuarioId(), Estados.NOTIF_POSTULACION_RECHAZADA, Map.of(
                        "licitacionId", String.valueOf(l.id()), "titulo", l.titulo(), "razonSocial", pyme.razonSocial(),
                        "motivo", "La licitación fue adjudicada a otra empresa."));
            }
        }
        return mapper.postulacion(resultado.aprobada(), ganadora, null);
    }

    @Override
    public PostulacionResponse rechazar(UsuarioActual u, Integer postulacionId) {
        PostulacionBdDto p = bd.postulacion(postulacionId);
        LicitacionBdDto l = bd.obtener(p.licitacionId());
        LicitacionServiceImpl.exigirDuenoOAdmin(u, l);
        if (!Estados.PENDIENTE.equals(p.estado())) {
            throw new BusinessException("POSTULACION_NO_PENDIENTE", "Solo se puede rechazar una postulación pendiente");
        }
        PostulacionBdDto rechazada = bd.cambiarEstadoPostulacion(postulacionId, new EstadoDto(Estados.RECHAZADA));
        PerfilDto pyme = perfilPymeSeguro(p.pymeId());
        if (pyme != null) {
            notificador.notificar(pyme.usuarioId(), Estados.NOTIF_POSTULACION_RECHAZADA, Map.of(
                    "licitacionId", String.valueOf(l.id()), "titulo", l.titulo(), "razonSocial", pyme.razonSocial(),
                    "motivo", "El Licitador revisó tu postulación y decidió no continuar con ella."));
        }
        return mapper.postulacion(rechazada, pyme, null);
    }

    @Override
    public UsoPlanResponse usoPlan(UsuarioActual u) {
        if (!u.esPyme()) {
            throw new ForbiddenException("El límite de postulaciones aplica solo a las Pymes");
        }
        PlanVigenteDto plan = ventas.planVigente(u.usuarioId());
        LocalDate inicioMes = Fechas.hoy().withDayOfMonth(1);
        long usadas = bd.contarPostulaciones(u.perfilId(), inicioMes).total();
        long restantes = plan.limitePostulacionesMes() == null ? Long.MAX_VALUE : Math.max(0, plan.limitePostulacionesMes() - usadas);
        return new UsoPlanResponse(plan.plan(), plan.premium(), usadas, plan.limitePostulacionesMes(),
                plan.limitePostulacionesMes() == null ? -1 : restantes);
    }

    @Override
    public ContextoPostulacionResponse contexto(Integer postulacionId) {
        PostulacionBdDto p = bd.postulacion(postulacionId);
        return new ContextoPostulacionResponse(p.id(), p.estado(), p.licitacionId(), p.licitacionTitulo(), p.licitadorId(), p.pymeId());
    }

    @Override
    public PaginaResponse<PostulacionResponse> adminListar(UsuarioActual admin, String estado, int page, int size) {
        if (!admin.esAdministrador()) {
            throw new ForbiddenException("Solo el Administrador puede realizar esta acción");
        }
        PaginaResponse<PostulacionBdDto> pagina = bd.postulaciones(null, null, estado, page, size);
        Map<Integer, PerfilDto> pymes = perfilesPymes(pagina.content());
        Map<Integer, String> licitadores = nombresLicitadores(pagina.content());
        return pagina.map(p -> mapper.postulacion(p, pymes.get(p.pymeId()), licitadores.get(p.licitadorId())));
    }

    // ------------------------------------------------------------------ apoyo

    private PerfilDto perfilPyme(Integer pymeId) {
        try {
            return usuarios.pyme(pymeId);
        } catch (RemoteServiceException e) {
            if (e.esNoEncontrado()) {
                throw new ForbiddenException("PYME_INVALIDA", "Tu perfil de Pyme no existe");
            }
            throw e;
        }
    }

    private PerfilDto perfilPymeSeguro(Integer pymeId) {
        try {
            return usuarios.pyme(pymeId);
        } catch (Exception e) {
            log.warn("No se pudo obtener la pyme {}: {}", pymeId, e.getMessage());
            return null;
        }
    }

    private Map<Integer, PerfilDto> perfilesPymes(List<PostulacionBdDto> lista) {
        List<Integer> ids = lista.stream().map(PostulacionBdDto::pymeId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return usuarios.pymes(ids).stream().collect(Collectors.toMap(PerfilDto::perfilId, Function.identity(), (a, b) -> a));
        } catch (Exception e) {
            log.warn("No se pudieron obtener los perfiles de las pymes: {}", e.getMessage());
            return Map.of();
        }
    }

    private Map<Integer, String> nombresLicitadores(List<PostulacionBdDto> lista) {
        List<Integer> ids = lista.stream().map(PostulacionBdDto::licitadorId).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        try {
            return usuarios.licitadores(ids).stream().collect(Collectors.toMap(PerfilDto::perfilId, PerfilDto::razonSocial, (a, b) -> a));
        } catch (Exception e) {
            log.warn("No se pudieron obtener los licitadores: {}", e.getMessage());
            return Map.of();
        }
    }
}
