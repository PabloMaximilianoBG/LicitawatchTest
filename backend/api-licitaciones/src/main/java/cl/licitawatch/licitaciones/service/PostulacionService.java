package cl.licitawatch.licitaciones.service;

import cl.licitawatch.licitaciones.dto.CrearPostulacionRequest;
import cl.licitawatch.licitaciones.dto.NotificacionRequest;
import cl.licitawatch.licitaciones.dto.PostulacionResponse;
import cl.licitawatch.licitaciones.entity.EstadoLicitacion;
import cl.licitawatch.licitaciones.entity.EstadoPostulacion;
import cl.licitawatch.licitaciones.entity.Licitacion;
import cl.licitawatch.licitaciones.entity.Postulacion;
import cl.licitawatch.licitaciones.exception.EstadoInvalidoException;
import cl.licitawatch.licitaciones.exception.OperacionNoPermitidaException;
import cl.licitawatch.licitaciones.exception.PostulacionDuplicadaException;
import cl.licitawatch.licitaciones.exception.RecursoNoEncontradoException;
import cl.licitawatch.licitaciones.repository.PostulacionRepository;
import cl.licitawatch.licitaciones.security.AuthenticatedUser;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostulacionService {

    private final PostulacionRepository postulacionRepository;
    private final LicitacionService licitacionService;
    private final NotificacionClient notificacionClient;

    public PostulacionService(
            PostulacionRepository postulacionRepository,
            LicitacionService licitacionService,
            NotificacionClient notificacionClient) {
        this.postulacionRepository = postulacionRepository;
        this.licitacionService = licitacionService;
        this.notificacionClient = notificacionClient;
    }

    @Transactional
    public PostulacionResponse postular(Long licitacionId, AuthenticatedUser usuario, CrearPostulacionRequest req) {
        Licitacion licitacion = licitacionService.buscar(licitacionId);
        if (licitacion.getEstado() != EstadoLicitacion.PUBLICADA) {
            throw new EstadoInvalidoException("Esta licitacion ya no esta publicada");
        }
        if (postulacionRepository.existsByLicitacion_IdAndClienteId(licitacionId, usuario.id())) {
            throw new PostulacionDuplicadaException();
        }

        Postulacion postulacion = new Postulacion();
        postulacion.setLicitacion(licitacion);
        postulacion.setClienteId(usuario.id());
        postulacion.setPropuesta(req.propuesta());
        postulacion.setEstado(EstadoPostulacion.ENVIADA);
        postulacionRepository.save(postulacion);

        notificacionClient.notificar(new NotificacionRequest(
                licitacion.getEmpresaId(), "LICITACION", "EMAIL",
                "Nueva postulacion recibida",
                "Tu licitacion \"" + licitacion.getTitulo() + "\" recibio una nueva postulacion."));

        return aRespuesta(postulacion);
    }

    @Transactional(readOnly = true)
    public List<PostulacionResponse> misPostulaciones(Long clienteId) {
        return postulacionRepository.findByClienteIdOrderByFechaPostulacionDesc(clienteId).stream()
                .map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<PostulacionResponse> postulacionesDeLicitacion(Long licitacionId, AuthenticatedUser usuario) {
        Licitacion licitacion = licitacionService.buscar(licitacionId);
        exigirDueno(licitacion, usuario);
        return postulacionRepository.findByLicitacion_IdOrderByFechaPostulacionDesc(licitacionId).stream()
                .map(this::aRespuesta).toList();
    }

    @Transactional
    public PostulacionResponse actualizarEstado(Long licitacionId, Long postulacionId, AuthenticatedUser usuario, EstadoPostulacion nuevoEstado) {
        Licitacion licitacion = licitacionService.buscar(licitacionId);
        exigirDueno(licitacion, usuario);

        Postulacion postulacion = postulacionRepository.findByIdAndLicitacion_Id(postulacionId, licitacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Postulacion no encontrada"));
        postulacion.setEstado(nuevoEstado);
        postulacionRepository.save(postulacion);
        return aRespuesta(postulacion);
    }

    private void exigirDueno(Licitacion licitacion, AuthenticatedUser usuario) {
        if (!licitacion.getEmpresaId().equals(usuario.id())) {
            throw new OperacionNoPermitidaException("Esta licitacion no te pertenece");
        }
    }

    private PostulacionResponse aRespuesta(Postulacion p) {
        return new PostulacionResponse(p.getId(), p.getLicitacion().getId(), p.getLicitacion().getTitulo(),
                p.getClienteId(), p.getFechaPostulacion(), p.getPropuesta(), p.getEstado().name());
    }
}
