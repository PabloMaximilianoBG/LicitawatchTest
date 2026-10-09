package cl.licitawatch.chat.bs.service.impl;

import cl.licitawatch.chat.bs.client.ChatBdClient;
import cl.licitawatch.chat.bs.client.LicitacionClient;
import cl.licitawatch.chat.bs.client.NotificacionClient;
import cl.licitawatch.chat.bs.client.UsuarioClient;
import cl.licitawatch.chat.bs.client.dto.*;
import cl.licitawatch.chat.bs.dto.request.MensajeRequest;
import cl.licitawatch.chat.bs.dto.response.ConversacionResponse;
import cl.licitawatch.chat.bs.dto.response.MensajeResponse;
import cl.licitawatch.chat.bs.service.ConversacionService;
import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.RemoteServiceException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversacionServiceImpl implements ConversacionService {
    static final String APROBADA = "Aprobada";
    static final String NOTIF_MENSAJE_NUEVO = "Mensaje nuevo";

    private final ChatBdClient bd;
    private final LicitacionClient licitaciones;
    private final UsuarioClient usuarios;
    private final NotificacionClient notificaciones;

    @Override
    public ConversacionResponse abrir(UsuarioActual u, Integer postulacionId) {
        if (!u.esLicitador()) {
            throw new ForbiddenException("Solo el Licitador puede iniciar el chat con la Pyme adjudicada");
        }
        ContextoPostulacionDto ctx = licitaciones.contexto(postulacionId);
        if (!Objects.equals(ctx.licitadorId(), u.perfilId())) {
            throw new ForbiddenException("La postulación no pertenece a una de tus licitaciones");
        }
        if (!APROBADA.equals(ctx.estado())) {
            throw new BusinessException("CHAT_NO_DISPONIBLE", "El chat se habilita cuando apruebas la postulación (adjudicación)");
        }
        ConversacionBdDto c;
        try {
            c = bd.porPostulacion(postulacionId, u.usuarioId());
        } catch (RemoteServiceException e) {
            if (!e.esNoEncontrado()) {
                throw e;
            }
            c = bd.crear(new ConversacionBdRequestDto(postulacionId, ctx.licitadorId(), ctx.pymeId()));
        }
        return enriquecer(u, c, ctx);
    }

    @Override
    public ConversacionResponse porPostulacion(UsuarioActual u, Integer postulacionId) {
        ConversacionBdDto c = bd.porPostulacion(postulacionId, u.usuarioId());
        exigirParticipante(u, c);
        return enriquecer(u, c, null);
    }

    @Override
    public ConversacionResponse obtener(UsuarioActual u, Integer conversacionId) {
        ConversacionBdDto c = bd.obtener(conversacionId, u.usuarioId());
        exigirParticipante(u, c);
        return enriquecer(u, c, null);
    }

    @Override
    public List<ConversacionResponse> mias(UsuarioActual u) {
        List<ConversacionBdDto> lista;
        if (u.esLicitador()) {
            lista = bd.listar(u.perfilId(), null, u.usuarioId());
        } else if (u.esPyme()) {
            lista = bd.listar(null, u.perfilId(), u.usuarioId());
        } else {
            throw new ForbiddenException("El chat es privado entre el Licitador y la Pyme");
        }
        return lista.stream().map(c -> enriquecer(u, c, null)).toList();
    }

    @Override
    public List<MensajeResponse> mensajes(UsuarioActual u, Integer conversacionId, Integer despuesDeId) {
        ConversacionBdDto c = bd.obtener(conversacionId, null);
        exigirParticipante(u, c);
        List<MensajeBdDto> lista = bd.mensajes(conversacionId, despuesDeId);
        if (lista.stream().anyMatch(m -> !m.emisorId().equals(u.usuarioId()) && !Boolean.TRUE.equals(m.leido()))) {
            bd.marcarLeidos(conversacionId, u.usuarioId());
        }
        return lista.stream().map(m -> new MensajeResponse(m.id(), m.conversacionId(), m.emisorId(),
                m.emisorId().equals(u.usuarioId()), m.contenido(), m.enviadoAt(), m.leido())).toList();
    }

    @Override
    public MensajeResponse enviar(UsuarioActual u, Integer conversacionId, MensajeRequest r) {
        ConversacionBdDto c = bd.obtener(conversacionId, null);
        exigirParticipante(u, c);
        PerfilDto contraparte = u.esLicitador() ? perfil(() -> usuarios.pyme(c.pymeId())) : perfil(() -> usuarios.licitador(c.licitadorId()));
        // Se avisa por correo solo si la contraparte no tenía mensajes sin leer (evita un correo por cada mensaje).
        long pendientesAntes = contraparte != null ? bd.noLeidos(conversacionId, contraparte.usuarioId()).total() : 1;
        MensajeBdDto m = bd.enviar(conversacionId, new MensajeBdRequestDto(u.usuarioId(), r.contenido()));
        if (contraparte != null && pendientesAntes == 0) {
            notificarMensaje(u, c, contraparte);
        }
        return new MensajeResponse(m.id(), m.conversacionId(), m.emisorId(), true, m.contenido(), m.enviadoAt(), m.leido());
    }

    @Override
    public int eliminarPorPostulaciones(List<Integer> postulacionIds) {
        return (int) bd.eliminar(postulacionIds).total();
    }

    // ------------------------------------------------------------------ apoyo

    /** ER pág. 6: solo el Licitador o la Pyme de la conversación pueden leer o escribir; cualquier otro es rechazado. */
    static void exigirParticipante(UsuarioActual u, ConversacionBdDto c) {
        boolean participa = (u.esLicitador() && Objects.equals(c.licitadorId(), u.perfilId()))
                || (u.esPyme() && Objects.equals(c.pymeId(), u.perfilId()));
        if (!participa) {
            throw new ForbiddenException("No participas en esta conversación");
        }
    }

    private void notificarMensaje(UsuarioActual u, ConversacionBdDto c, PerfilDto contraparte) {
        try {
            String titulo = "";
            try {
                titulo = licitaciones.contexto(c.postulacionId()).licitacionTitulo();
            } catch (Exception ignored) {
                // el título es informativo
            }
            PerfilDto yo = u.esLicitador() ? perfil(() -> usuarios.licitador(c.licitadorId())) : perfil(() -> usuarios.pyme(c.pymeId()));
            notificaciones.notificar(new NotificacionDto(contraparte.usuarioId(), NOTIF_MENSAJE_NUEVO, Map.of(
                    "remitente", yo != null && yo.razonSocial() != null ? yo.razonSocial() : u.email(),
                    "titulo", titulo == null ? "" : titulo, "conversacionId", String.valueOf(c.id()))));
        } catch (Exception e) {
            log.warn("No se pudo notificar el mensaje nuevo de la conversación {}: {}", c.id(), e.getMessage());
        }
    }

    private ConversacionResponse enriquecer(UsuarioActual u, ConversacionBdDto c, ContextoPostulacionDto ctx) {
        ContextoPostulacionDto contexto = ctx;
        if (contexto == null) {
            try {
                contexto = licitaciones.contexto(c.postulacionId());
            } catch (Exception e) {
                log.debug("Contexto de la postulación {} no disponible", c.postulacionId());
            }
        }
        PerfilDto licitador = perfil(() -> usuarios.licitador(c.licitadorId()));
        PerfilDto pyme = perfil(() -> usuarios.pyme(c.pymeId()));
        String licitadorNombre = licitador != null ? licitador.razonSocial() : null;
        String pymeNombre = pyme != null ? pyme.razonSocial() : null;
        return ConversacionResponse.builder().id(c.id()).postulacionId(c.postulacionId())
                .licitacionId(contexto != null ? contexto.licitacionId() : null)
                .licitacionTitulo(contexto != null ? contexto.licitacionTitulo() : null)
                .licitadorId(c.licitadorId()).licitadorNombre(licitadorNombre).pymeId(c.pymeId()).pymeNombre(pymeNombre)
                .pymePremium(pyme != null && pyme.premium()).contraparteNombre(u.esLicitador() ? pymeNombre : licitadorNombre)
                .createdAt(c.createdAt()).ultimoMensaje(c.ultimoMensaje()).ultimoMensajeAt(c.ultimoMensajeAt()).noLeidos(c.noLeidos())
                .build();
    }

    private static PerfilDto perfil(java.util.function.Supplier<PerfilDto> s) {
        try {
            return s.get();
        } catch (Exception e) {
            return null;
        }
    }
}
