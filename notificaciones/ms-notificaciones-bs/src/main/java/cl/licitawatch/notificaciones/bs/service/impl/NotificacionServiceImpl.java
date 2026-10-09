package cl.licitawatch.notificaciones.bs.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.notificaciones.bs.client.NotificacionBdClient;
import cl.licitawatch.notificaciones.bs.client.UsuarioClient;
import cl.licitawatch.notificaciones.bs.client.VentasClient;
import cl.licitawatch.notificaciones.bs.client.dto.CatalogoDto;
import cl.licitawatch.notificaciones.bs.client.dto.NotificacionBdDto;
import cl.licitawatch.notificaciones.bs.client.dto.NotificacionBdRequestDto;
import cl.licitawatch.notificaciones.bs.client.dto.UsuarioDto;
import cl.licitawatch.notificaciones.bs.dto.request.CorreoEnlaceRequest;
import cl.licitawatch.notificaciones.bs.dto.request.NotificacionRequest;
import cl.licitawatch.notificaciones.bs.dto.request.SoporteRequest;
import cl.licitawatch.notificaciones.bs.dto.response.NotificacionResponse;
import cl.licitawatch.notificaciones.bs.dto.response.SoporteResponse;
import cl.licitawatch.notificaciones.bs.service.ContenidoCorreoFactory;
import cl.licitawatch.notificaciones.bs.service.CorreoService;
import cl.licitawatch.notificaciones.bs.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {
    static final String CANAL = "email";

    private final NotificacionBdClient bd;
    private final UsuarioClient usuarios;
    private final VentasClient ventas;
    private final CorreoService correo;
    private final ContenidoCorreoFactory contenidos;

    @Value("${licitawatch.mail.soporte}")
    private String casillaSoporte;

    @Override
    public NotificacionResponse notificar(NotificacionRequest r) {
        if (bd.tipos().stream().map(CatalogoDto::nombre).noneMatch(r.tipo()::equals)) {
            throw new BadRequestException("TIPO_INVALIDO", "Tipo de notificación inválido: " + r.tipo());
        }
        UsuarioDto destinatario = usuario(r.usuarioId());
        boolean enviado = false;
        try {
            correo.enviar(destinatario.email(), contenidos.evento(r.tipo(), destinatario.nombre(), r.datos()));
            enviado = true;
        } catch (Exception e) {
            log.warn("Notificación '{}' para {} registrada sin correo: {}", r.tipo(), r.usuarioId(), e.getMessage());
        }
        NotificacionBdDto n = bd.registrar(new NotificacionBdRequestDto(r.usuarioId(), r.tipo(), CANAL));
        return new NotificacionResponse(n.id(), n.usuarioId(), destinatario.email(), n.tipo(), n.canal(), n.createdAt(), enviado);
    }

    @Override
    public void confirmacionCuenta(CorreoEnlaceRequest r) {
        correo.enviar(r.email(), contenidos.confirmacionCuenta(r.nombre(), r.enlace()));
    }

    @Override
    public void restablecerPassword(CorreoEnlaceRequest r) {
        correo.enviar(r.email(), contenidos.restablecerPassword(r.nombre(), r.enlace()));
    }

    /** Soporte estándar (Estándar y Licitador) o prioritario (Pyme Premium): correo marcado a la casilla de soporte. */
    @Override
    public SoporteResponse soporte(UsuarioActual u, SoporteRequest r) {
        if (u.esAdministrador()) {
            throw new ForbiddenException("El soporte es para Licitadores y Pymes");
        }
        UsuarioDto usuario = usuario(u.usuarioId());
        boolean prioritario = false;
        if (u.esPyme()) {
            try {
                prioritario = ventas.planVigente(u.usuarioId()).premium();
            } catch (Exception e) {
                log.warn("No se pudo consultar el plan para soporte: {}", e.getMessage());
            }
        }
        correo.enviarSoporte(casillaSoporte, usuario.email(), prioritario,
                contenidos.soporte(usuario.nombre(), usuario.email(), u.rol(), prioritario, r.asunto().trim(), r.mensaje().trim()));
        try {
            correo.enviar(usuario.email(), contenidos.acuseSoporte(usuario.nombre(), prioritario, r.asunto().trim()));
        } catch (Exception e) {
            log.warn("No se pudo enviar el acuse de soporte: {}", e.getMessage());
        }
        return new SoporteResponse(prioritario ? "Prioritario" : "Estándar",
                "Recibimos tu solicitud" + (prioritario ? " con prioridad Premium." : ".") + " Te responderemos a " + usuario.email());
    }

    @Override
    public PaginaResponse<NotificacionResponse> mias(UsuarioActual u, int page, int size) {
        return bd.listar(u.usuarioId(), null, page, size)
                .map(n -> new NotificacionResponse(n.id(), n.usuarioId(), u.email(), n.tipo(), n.canal(), n.createdAt(), null));
    }

    @Override
    public PaginaResponse<NotificacionResponse> adminListar(UsuarioActual admin, String tipo, int page, int size) {
        if (!admin.esAdministrador()) {
            throw new ForbiddenException("Solo el Administrador puede realizar esta acción");
        }
        PaginaResponse<NotificacionBdDto> pagina = bd.listar(null, tipo, page, size);
        Map<Integer, String> correos = new HashMap<>();
        for (Integer id : new LinkedHashSet<>(pagina.content().stream().map(NotificacionBdDto::usuarioId).toList())) {
            try {
                correos.put(id, usuarios.usuario(id).email());
            } catch (Exception e) {
                log.debug("Usuario {} no disponible", id);
            }
        }
        return pagina.map(n -> new NotificacionResponse(n.id(), n.usuarioId(), correos.get(n.usuarioId()), n.tipo(), n.canal(), n.createdAt(), null));
    }

    private UsuarioDto usuario(Integer id) {
        try {
            return usuarios.usuario(id);
        } catch (RemoteServiceException e) {
            if (e.esNoEncontrado()) {
                throw new BadRequestException("USUARIO_INVALIDO", "El usuario destinatario no existe");
            }
            throw e;
        }
    }
}
