package cl.licitawatch.ventas.bs.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.ventas.bs.client.UsuarioClient;
import cl.licitawatch.ventas.bs.client.VentasBdClient;
import cl.licitawatch.ventas.bs.client.dto.*;
import cl.licitawatch.ventas.bs.config.PlanesProperties;
import cl.licitawatch.ventas.bs.dto.response.MiSuscripcionResponse;
import cl.licitawatch.ventas.bs.dto.response.PlanVigenteResponse;
import cl.licitawatch.ventas.bs.dto.response.SuscripcionResponse;
import cl.licitawatch.ventas.bs.mapper.VentasMapper;
import cl.licitawatch.ventas.bs.service.ClienteService;
import cl.licitawatch.ventas.bs.service.SuscripcionService;
import cl.licitawatch.ventas.bs.util.Catalogos;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * PPT diap. 7: la Pyme tiene Estándar (gratis, por defecto) o Premium (pagado). La vigencia de Premium se calcula
 * desde la fecha de la venta pagada + PREMIUM_VIGENCIA_DIAS (el ER no tiene columnas de fechas en suscripcion).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuscripcionServiceImpl implements SuscripcionService {
    private final VentasBdClient bd;
    private final UsuarioClient usuarios;
    private final ClienteService clienteService;
    private final PlanesProperties planes;
    private final VentasMapper mapper;

    @Override
    public MiSuscripcionResponse miSuscripcion(UsuarioActual u) {
        exigirPyme(u);
        PlanVigenteResponse vigente = planVigente(u.usuarioId());
        SuscripcionBdDto activa = activa(u.usuarioId()).orElse(null);
        List<VentaBdDto> pendientes = bd.ventasPendientesPremium(u.usuarioId());
        LocalDate inicio = activa != null && vigente.premium() ? activa.fechaUltimoPagoAprobado() : null;
        LocalDate vence = inicio != null ? inicio.plusDays(planes.premium().vigenciaDias()) : null;
        return MiSuscripcionResponse.builder()
                .suscripcionId(activa != null ? activa.id() : null).plan(vigente.plan())
                .estado(activa != null ? activa.estado() : Catalogos.ACTIVA).premium(vigente.premium())
                .fechaInicio(inicio).fechaVencimiento(vence)
                .diasRestantes(vence != null ? Math.max(0, ChronoUnit.DAYS.between(Catalogos.hoy(), vence)) : null)
                .limitePostulacionesMes(vigente.limitePostulacionesMes()).accesoLicitasist(vigente.premium())
                .soporte(vigente.premium() ? "Prioritario" : "Estándar").precioPremium(planes.premium().precio())
                .ventaPendiente(!vigente.premium() && !pendientes.isEmpty() ? mapper.venta(pendientes.get(0), null) : null)
                .beneficios(vigente.premium() ? planes.premium().beneficios() : planes.estandar().beneficios())
                .build();
    }

    @Override
    public void asignarEstandar(Integer usuarioId) {
        try {
            UsuarioDto usuario = usuarios.usuario(usuarioId);
            if (!"PYME".equals(usuario.rol())) {
                throw new BusinessException("SOLO_PYMES", "Los planes de suscripción son solo para Pymes");
            }
        } catch (RemoteServiceException e) {
            if (e.esNoEncontrado()) {
                throw new BadRequestException("USUARIO_INVALIDO", "El usuario no existe");
            }
            throw e;
        }
        asegurarEstandar(usuarioId);
    }

    @Override
    public PlanVigenteResponse planVigente(Integer usuarioId) {
        Optional<SuscripcionBdDto> activa = activa(usuarioId);
        if (activa.isPresent() && Catalogos.PREMIUM.equals(activa.get().plan())) {
            if (!expirada(activa.get())) {
                return new PlanVigenteResponse(Catalogos.PREMIUM, true, planes.premium().postulacionesMes());
            }
            vencer(activa.get());
        } else if (activa.isEmpty()) {
            asegurarEstandar(usuarioId);
        }
        return new PlanVigenteResponse(Catalogos.ESTANDAR, false, planes.estandar().postulacionesMes());
    }

    @Override
    public List<Integer> usuariosPremium(List<Integer> usuarioIds) {
        if (usuarioIds == null || usuarioIds.isEmpty()) {
            return List.of();
        }
        Set<Integer> premium = new HashSet<>(bd.usuariosPremium(usuarioIds));
        // Descarta las que ya vencieron aunque el job diario aún no corra
        premium.removeIf(id -> activa(id).map(this::expirada).orElse(true));
        return List.copyOf(premium);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void alIniciar() {
        try {
            vencerPremiumExpiradas();
        } catch (Exception e) {
            log.warn("Revisión de vencimientos no ejecutada: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "${licitawatch.vencimiento.cron}", zone = "America/Santiago")
    public void programado() {
        alIniciar();
    }

    @Override
    public int vencerPremiumExpiradas() {
        int total = 0;
        for (SuscripcionBdDto s : bd.premiumActivas()) {
            if (expirada(s)) {
                vencer(s);
                total++;
            }
        }
        if (total > 0) {
            log.info("{} suscripciones Premium pasaron a Vencida", total);
        }
        return total;
    }

    @Override
    public PaginaResponse<SuscripcionResponse> adminListar(UsuarioActual admin, String estado, String plan, int page, int size) {
        exigirAdmin(admin);
        PaginaResponse<SuscripcionBdDto> pagina = bd.suscripciones(null, estado, plan, page, size);
        Map<Integer, UsuarioDto> clientes = clienteService.clientes(pagina.content().stream().map(SuscripcionBdDto::usuarioId).toList());
        return pagina.map(s -> mapper.suscripcion(s, clientes.get(s.usuarioId()), planes.premium().vigenciaDias()));
    }

    @Override
    public SuscripcionResponse adminCancelar(UsuarioActual admin, Integer suscripcionId) {
        exigirAdmin(admin);
        SuscripcionBdDto s = bd.suscripcion(suscripcionId);
        if (!Catalogos.PREMIUM.equals(s.plan()) || !Catalogos.ACTIVA.equals(s.estado())) {
            throw new BusinessException("SUSCRIPCION_NO_CANCELABLE", "Solo se puede cancelar una suscripción Premium activa");
        }
        SuscripcionBdDto cancelada = bd.cambiarEstadoSuscripcion(suscripcionId, new EstadoDto(Catalogos.CANCELADA));
        asegurarEstandar(s.usuarioId());
        return mapper.suscripcion(cancelada, clienteService.clientes(List.of(s.usuarioId())).get(s.usuarioId()), planes.premium().vigenciaDias());
    }

    // ------------------------------------------------------------------ apoyo

    private Optional<SuscripcionBdDto> activa(Integer usuarioId) {
        List<SuscripcionBdDto> activas = bd.suscripcionesDeUsuario(usuarioId).stream()
                .filter(s -> Catalogos.ACTIVA.equals(s.estado())).toList();
        return activas.stream().filter(s -> Catalogos.PREMIUM.equals(s.plan())).findFirst().or(() -> activas.stream().findFirst());
    }

    private boolean expirada(SuscripcionBdDto s) {
        if (!Catalogos.PREMIUM.equals(s.plan())) {
            return false;
        }
        return s.fechaUltimoPagoAprobado() == null
                || s.fechaUltimoPagoAprobado().plusDays(planes.premium().vigenciaDias()).isBefore(Catalogos.hoy());
    }

    private void vencer(SuscripcionBdDto s) {
        bd.cambiarEstadoSuscripcion(s.id(), new EstadoDto(Catalogos.VENCIDA));
        asegurarEstandar(s.usuarioId());
    }

    private void asegurarEstandar(Integer usuarioId) {
        boolean tieneEstandarActiva = bd.suscripcionesDeUsuario(usuarioId).stream()
                .anyMatch(s -> Catalogos.ESTANDAR.equals(s.plan()) && Catalogos.ACTIVA.equals(s.estado()));
        if (!tieneEstandarActiva) {
            bd.crearSuscripcion(new SuscripcionBdRequestDto(usuarioId, Catalogos.ESTANDAR, Catalogos.ACTIVA));
        }
    }

    static void exigirPyme(UsuarioActual u) {
        if (!u.esPyme()) {
            throw new ForbiddenException("SOLO_PYMES", "Los planes de suscripción son solo para Pymes. El Licitador publica sin costo.");
        }
    }

    static void exigirAdmin(UsuarioActual u) {
        if (!u.esAdministrador()) {
            throw new ForbiddenException("Solo el Administrador puede realizar esta acción");
        }
    }
}
