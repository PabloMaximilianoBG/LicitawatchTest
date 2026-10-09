package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import cl.licitawatch.common.seguridad.Roles;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.client.ChatClient;
import cl.licitawatch.licitaciones.bs.client.LicitacionBdClient;
import cl.licitawatch.licitaciones.bs.client.UsuarioClient;
import cl.licitawatch.licitaciones.bs.client.dto.*;
import cl.licitawatch.licitaciones.bs.dto.request.BusquedaRequest;
import cl.licitawatch.licitaciones.bs.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bs.dto.response.ArchivoDescarga;
import cl.licitawatch.licitaciones.bs.dto.response.CatalogosResponse;
import cl.licitawatch.licitaciones.bs.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bs.mapper.LicitacionMapper;
import cl.licitawatch.licitaciones.bs.service.AlmacenamientoService;
import cl.licitawatch.licitaciones.bs.service.CatalogoNombresService;
import cl.licitawatch.licitaciones.bs.service.LicitacionService;
import cl.licitawatch.licitaciones.bs.service.NotificadorService;
import cl.licitawatch.licitaciones.bs.util.Estados;
import cl.licitawatch.licitaciones.bs.util.Fechas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LicitacionServiceImpl implements LicitacionService {
    private final LicitacionBdClient bd;
    private final UsuarioClient usuarios;
    private final ChatClient chat;
    private final CatalogoNombresService nombres;
    private final NotificadorService notificador;
    private final AlmacenamientoService almacenamiento;
    private final LicitacionMapper mapper;

    @Override
    public PaginaResponse<LicitacionResponse> buscar(UsuarioActual u, BusquedaRequest f) {
        // Pyme y Licitador ven las licitaciones abiertas y vigentes; las que alcanzaron su máximo de postulantes
        // dejan de estar disponibles (decisión del cliente).
        PaginaResponse<LicitacionBdDto> pagina = bd.buscar(f.q(), f.rubroId(), f.regionId(), Estados.ABIERTA, null,
                f.presupuestoMin(), f.presupuestoMax(), Fechas.hoy(), true, f.orden(), f.page(), f.size());
        return enriquecer(u, pagina);
    }

    @Override
    public List<LicitacionResponse> misLicitaciones(UsuarioActual u) {
        exigirLicitador(u);
        PaginaResponse<LicitacionBdDto> pagina = bd.buscar(null, null, null, null, u.perfilId(), null, null, null, null,
                null, 0, 100);
        return enriquecer(u, pagina).content();
    }

    @Override
    public LicitacionResponse obtener(UsuarioActual u, Integer id) {
        LicitacionBdDto l = bd.obtener(id);
        PostulacionBdDto mia = null;
        if (u.esPyme()) {
            mia = bd.postulaciones(id, u.perfilId(), null, 0, 1).content().stream().findFirst().orElse(null);
        }
        boolean visible = u.esAdministrador() || Estados.ABIERTA.equals(l.estado()) || esDueno(u, l) || mia != null;
        if (!visible) {
            throw new ResourceNotFoundException("LICITACION_NO_ENCONTRADA", "La licitación no existe o ya no está disponible");
        }
        CatalogoNombresService.Nombres n = nombres.para(List.of(l));
        return mapper.licitacion(l, n.licitadores().get(l.licitadorId()), n.rubros().get(l.rubroId()), n.regiones().get(l.regionId()), mia);
    }

    @Override
    public LicitacionResponse crear(UsuarioActual u, LicitacionRequest r) {
        exigirLicitador(u);
        validar(r, null);
        nombres.validarRubroYRegion(r.rubroId(), r.regionId());
        validarLicitador(u.perfilId());
        LicitacionBdDto creada = bd.crear(LicitacionBdRequestDto.builder().licitadorId(u.perfilId()).titulo(r.titulo())
                .descripcion(r.descripcion()).rubroId(r.rubroId()).regionId(r.regionId()).presupuestoMin(r.presupuestoMin())
                .presupuestoMax(r.presupuestoMax()).maxPostulantes(r.maxPostulantes()).fechaCierre(r.fechaCierre())
                .estado(Estados.ABIERTA).build());
        CatalogoNombresService.Nombres n = nombres.para(List.of(creada));
        notificador.alertarPymesPremiumDelRubro(creada, n.rubros().get(creada.rubroId()), n.regiones().get(creada.regionId()));
        return mapper.licitacion(creada, n.licitadores().get(creada.licitadorId()), n.rubros().get(creada.rubroId()),
                n.regiones().get(creada.regionId()), null);
    }

    @Override
    public LicitacionResponse actualizar(UsuarioActual u, Integer id, LicitacionRequest r) {
        LicitacionBdDto actual = bd.obtener(id);
        exigirDuenoOAdmin(u, actual);
        if (!Estados.ABIERTA.equals(actual.estado())) {
            throw new BusinessException("LICITACION_NO_EDITABLE", "Solo se pueden editar licitaciones abiertas");
        }
        validar(r, actual);
        nombres.validarRubroYRegion(r.rubroId(), r.regionId());
        LicitacionBdDto l = bd.actualizar(id, LicitacionBdRequestDto.builder().licitadorId(actual.licitadorId()).titulo(r.titulo())
                .descripcion(r.descripcion()).rubroId(r.rubroId()).regionId(r.regionId()).presupuestoMin(r.presupuestoMin())
                .presupuestoMax(r.presupuestoMax()).maxPostulantes(r.maxPostulantes()).fechaCierre(r.fechaCierre()).build());
        return unica(l);
    }

    @Override
    public LicitacionResponse cerrar(UsuarioActual u, Integer id) {
        LicitacionBdDto l = bd.obtener(id);
        exigirDuenoOAdmin(u, l);
        if (!Estados.ABIERTA.equals(l.estado())) {
            throw new BusinessException("LICITACION_NO_ABIERTA", "Solo se puede cerrar una licitación abierta");
        }
        return unica(bd.cambiarEstado(id, new EstadoDto(Estados.CERRADA)));
    }

    @Override
    public void eliminar(UsuarioActual u, Integer id) {
        LicitacionBdDto l = bd.obtener(id);
        exigirDuenoOAdmin(u, l);
        EliminacionBdDto eliminada = bd.eliminar(id);
        almacenamiento.eliminarTodo(id);
        if (!eliminada.postulacionesEliminadas().isEmpty()) {
            try {
                chat.eliminarPorPostulaciones(eliminada.postulacionesEliminadas());
            } catch (Exception e) {
                log.warn("No se pudieron eliminar las conversaciones de la licitación {}: {}", id, e.getMessage());
            }
        }
    }

    @Override
    public LicitacionResponse subirImagen(UsuarioActual u, Integer id, MultipartFile archivo) {
        LicitacionBdDto l = bd.obtener(id);
        exigirDuenoOAdmin(u, l);
        AlmacenamientoService.Guardado g = almacenamiento.guardarImagen(id, archivo);
        LicitacionBdDto actualizada = bd.actualizarArchivos(id, ArchivosBdDto.builder().imagenUrl(g.url()).build());
        almacenamiento.eliminarPorUrl(id, l.imagenUrl());
        return unica(actualizada);
    }

    @Override
    public LicitacionResponse subirDocumento(UsuarioActual u, Integer id, MultipartFile archivo) {
        LicitacionBdDto l = bd.obtener(id);
        exigirDuenoOAdmin(u, l);
        AlmacenamientoService.Guardado g = almacenamiento.guardarDocumento(id, archivo);
        LicitacionBdDto actualizada = bd.actualizarArchivos(id, ArchivosBdDto.builder().archivoUrl(g.url())
                .archivoNombre(g.nombreOriginal()).tipoArchivo(g.tipoArchivo()).build());
        almacenamiento.eliminarPorUrl(id, l.archivoUrl());
        return unica(actualizada);
    }

    @Override
    public LicitacionResponse quitarImagen(UsuarioActual u, Integer id) {
        LicitacionBdDto l = bd.obtener(id);
        exigirDuenoOAdmin(u, l);
        LicitacionBdDto actualizada = bd.actualizarArchivos(id, ArchivosBdDto.builder().limpiarImagen(true).build());
        almacenamiento.eliminarPorUrl(id, l.imagenUrl());
        return unica(actualizada);
    }

    @Override
    public LicitacionResponse quitarDocumento(UsuarioActual u, Integer id) {
        LicitacionBdDto l = bd.obtener(id);
        exigirDuenoOAdmin(u, l);
        LicitacionBdDto actualizada = bd.actualizarArchivos(id, ArchivosBdDto.builder().limpiarArchivo(true).build());
        almacenamiento.eliminarPorUrl(id, l.archivoUrl());
        return unica(actualizada);
    }

    @Override
    public ArchivoDescarga archivo(Integer licitacionId, String nombre) {
        return almacenamiento.leer(licitacionId, nombre);
    }

    @Override
    public CatalogosResponse catalogos() {
        CatalogosBdDto c = bd.catalogos();
        return new CatalogosResponse(c.estadosLicitacion(), c.estadosPostulacion(), c.tiposArchivo());
    }

    @Override
    public PaginaResponse<LicitacionResponse> adminListar(UsuarioActual admin, BusquedaRequest f) {
        exigirAdmin(admin);
        return enriquecer(admin, bd.buscar(f.q(), f.rubroId(), f.regionId(), f.estado(), null, f.presupuestoMin(),
                f.presupuestoMax(), null, null, f.orden(), f.page(), f.size()));
    }

    /** Moderación: el Administrador puede cerrar o reabrir (si la fecha de cierre sigue vigente). */
    @Override
    public LicitacionResponse adminCambiarEstado(UsuarioActual admin, Integer id, String estado) {
        exigirAdmin(admin);
        LicitacionBdDto l = bd.obtener(id);
        if (Estados.ADJUDICADA.equals(l.estado())) {
            throw new BusinessException("LICITACION_ADJUDICADA", "Una licitación adjudicada no cambia de estado");
        }
        if (Estados.ABIERTA.equalsIgnoreCase(estado)) {
            if (l.fechaCierre().isBefore(Fechas.hoy())) {
                throw new BusinessException("FECHA_CIERRE_VENCIDA", "Para reabrirla, primero actualiza la fecha de cierre");
            }
            return unica(bd.cambiarEstado(id, new EstadoDto(Estados.ABIERTA)));
        }
        if (Estados.CERRADA.equalsIgnoreCase(estado)) {
            return unica(bd.cambiarEstado(id, new EstadoDto(Estados.CERRADA)));
        }
        throw new BadRequestException("ESTADO_INVALIDO", "Estado inválido: usa Abierta o Cerrada");
    }

    // ------------------------------------------------------------------ apoyo

    private PaginaResponse<LicitacionResponse> enriquecer(UsuarioActual u, PaginaResponse<LicitacionBdDto> pagina) {
        CatalogoNombresService.Nombres n = nombres.para(pagina.content());
        Map<Integer, PostulacionBdDto> mias = new HashMap<>();
        if (u.esPyme() && !pagina.content().isEmpty()) {
            bd.postulaciones(null, u.perfilId(), null, 0, 200).content().forEach(p -> mias.put(p.licitacionId(), p));
        }
        return pagina.map(l -> mapper.licitacion(l, n.licitadores().get(l.licitadorId()), n.rubros().get(l.rubroId()),
                n.regiones().get(l.regionId()), mias.get(l.id())));
    }

    private LicitacionResponse unica(LicitacionBdDto l) {
        CatalogoNombresService.Nombres n = nombres.para(List.of(l));
        return mapper.licitacion(l, n.licitadores().get(l.licitadorId()), n.rubros().get(l.rubroId()), n.regiones().get(l.regionId()), null);
    }

    static void validar(LicitacionRequest r, LicitacionBdDto actual) {
        if (!r.fechaCierre().isAfter(Fechas.hoy().minusDays(actual == null ? 0 : 1))) {
            throw new BadRequestException("FECHA_CIERRE_INVALIDA", actual == null
                    ? "La fecha de cierre debe ser posterior a hoy" : "La fecha de cierre no puede quedar en el pasado");
        }
        if (r.presupuestoMin() != null && r.presupuestoMax() != null && r.presupuestoMin().compareTo(r.presupuestoMax()) > 0) {
            throw new BadRequestException("PRESUPUESTO_INVALIDO", "El presupuesto mínimo no puede ser mayor que el máximo");
        }
        if (actual != null && r.maxPostulantes() != null && r.maxPostulantes() < actual.cantidadPostulaciones()) {
            throw new BusinessException("MAX_POSTULANTES_INVALIDO", "Ya hay " + actual.cantidadPostulaciones()
                    + " postulaciones: el máximo no puede ser menor");
        }
    }

    private void validarLicitador(Integer licitadorId) {
        try {
            usuarios.licitador(licitadorId);
        } catch (RemoteServiceException e) {
            if (e.esNoEncontrado()) {
                throw new ForbiddenException("LICITADOR_INVALIDO", "Tu perfil de Licitador no existe");
            }
            throw e;
        }
    }

    private static boolean esDueno(UsuarioActual u, LicitacionBdDto l) {
        return u.esLicitador() && Objects.equals(u.perfilId(), l.licitadorId());
    }

    static void exigirLicitador(UsuarioActual u) {
        if (!Roles.LICITADOR.equals(u.rol())) {
            throw new ForbiddenException("Solo los Licitadores pueden realizar esta acción");
        }
    }

    private static void exigirAdmin(UsuarioActual u) {
        if (!u.esAdministrador()) {
            throw new ForbiddenException("Solo el Administrador puede realizar esta acción");
        }
    }

    static void exigirDuenoOAdmin(UsuarioActual u, LicitacionBdDto l) {
        if (!u.esAdministrador() && !esDueno(u, l)) {
            throw new ForbiddenException("Solo el Licitador dueño de la licitación puede realizar esta acción");
        }
    }
}
