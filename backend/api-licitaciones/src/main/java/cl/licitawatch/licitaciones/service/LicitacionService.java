package cl.licitawatch.licitaciones.service;

import cl.licitawatch.licitaciones.dto.ActualizarLicitacionRequest;
import cl.licitawatch.licitaciones.dto.CrearLicitacionRequest;
import cl.licitawatch.licitaciones.dto.LicitacionResponse;
import cl.licitawatch.licitaciones.dto.NotificacionRequest;
import cl.licitawatch.licitaciones.entity.EstadoLicitacion;
import cl.licitawatch.licitaciones.entity.Licitacion;
import cl.licitawatch.licitaciones.exception.EstadoInvalidoException;
import cl.licitawatch.licitaciones.exception.OperacionNoPermitidaException;
import cl.licitawatch.licitaciones.exception.RecursoNoEncontradoException;
import cl.licitawatch.licitaciones.repository.LicitacionRepository;
import cl.licitawatch.licitaciones.repository.LicitacionSpecifications;
import cl.licitawatch.licitaciones.security.AuthenticatedUser;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LicitacionService {

    private final LicitacionRepository licitacionRepository;
    private final NotificacionClient notificacionClient;

    public LicitacionService(LicitacionRepository licitacionRepository, NotificacionClient notificacionClient) {
        this.licitacionRepository = licitacionRepository;
        this.notificacionClient = notificacionClient;
    }

    @Transactional
    public LicitacionResponse crear(AuthenticatedUser usuario, CrearLicitacionRequest req) {
        Licitacion licitacion = new Licitacion();
        licitacion.setEmpresaId(usuario.id());
        licitacion.setTitulo(req.titulo());
        licitacion.setRubro(req.rubro());
        licitacion.setMontoEstimado(req.montoEstimado());
        licitacion.setRegion(req.region());
        licitacion.setFechaCierre(req.fechaCierre());
        licitacion.setEstado(EstadoLicitacion.PUBLICADA);
        licitacionRepository.save(licitacion);

        notificacionClient.notificar(new NotificacionRequest(
                usuario.id(), "LICITACION", "EMAIL",
                "Licitacion publicada",
                "Tu licitacion \"" + licitacion.getTitulo() + "\" fue publicada correctamente."));

        return aRespuesta(licitacion);
    }

    @Transactional
    public LicitacionResponse actualizar(Long id, AuthenticatedUser usuario, ActualizarLicitacionRequest req) {
        Licitacion licitacion = buscarPropia(id, usuario);
        if (licitacion.getEstado() != EstadoLicitacion.PUBLICADA) {
            throw new EstadoInvalidoException("Solo se puede editar una licitacion mientras esta publicada");
        }
        licitacion.setTitulo(req.titulo());
        licitacion.setRubro(req.rubro());
        licitacion.setMontoEstimado(req.montoEstimado());
        licitacion.setRegion(req.region());
        licitacion.setFechaCierre(req.fechaCierre());
        licitacionRepository.save(licitacion);
        return aRespuesta(licitacion);
    }

    @Transactional
    public LicitacionResponse cerrar(Long id, AuthenticatedUser usuario) {
        Licitacion licitacion = buscarPropia(id, usuario);
        licitacion.setEstado(EstadoLicitacion.CERRADA);
        licitacionRepository.save(licitacion);
        return aRespuesta(licitacion);
    }

    @Transactional(readOnly = true)
    public LicitacionResponse obtener(Long id) {
        return aRespuesta(buscar(id));
    }

    @Transactional(readOnly = true)
    public List<LicitacionResponse> buscarPublicadas(String rubro, String region) {
        Specification<Licitacion> spec = Specification
                .where(LicitacionSpecifications.conEstado(EstadoLicitacion.PUBLICADA))
                .and(LicitacionSpecifications.conRubro(rubro))
                .and(LicitacionSpecifications.conRegion(region));
        return licitacionRepository.findAll(spec).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<LicitacionResponse> misLicitaciones(Long empresaId) {
        return licitacionRepository.findByEmpresaIdOrderByCreadoEnDesc(empresaId).stream().map(this::aRespuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<LicitacionResponse> listarTodas() {
        return licitacionRepository.findAll().stream().map(this::aRespuesta).toList();
    }

    Licitacion buscar(Long id) {
        return licitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Licitacion no encontrada"));
    }

    private Licitacion buscarPropia(Long id, AuthenticatedUser usuario) {
        Licitacion licitacion = buscar(id);
        if (!licitacion.getEmpresaId().equals(usuario.id())) {
            throw new OperacionNoPermitidaException("Esta licitacion no te pertenece");
        }
        return licitacion;
    }

    private LicitacionResponse aRespuesta(Licitacion l) {
        return new LicitacionResponse(l.getId(), l.getEmpresaId(), l.getTitulo(), l.getRubro(),
                l.getMontoEstimado(), l.getRegion(), l.getEstado().name(), l.getFechaCierre());
    }
}
