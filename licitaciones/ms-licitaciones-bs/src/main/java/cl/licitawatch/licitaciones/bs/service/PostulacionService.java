package cl.licitawatch.licitaciones.bs.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bs.dto.response.ContextoPostulacionResponse;
import cl.licitawatch.licitaciones.bs.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bs.dto.response.UsoPlanResponse;

import java.util.List;

/** PPT diap. 6 y 8: la Pyme postula y ve el estado; el Licitador revisa, aprueba o rechaza y adjudica. */
public interface PostulacionService {
    PostulacionResponse postular(UsuarioActual pyme, Integer licitacionId, PostularRequest request);

    List<PostulacionResponse> misPostulaciones(UsuarioActual pyme);

    List<PostulacionResponse> postulantes(UsuarioActual usuario, Integer licitacionId);

    PostulacionResponse aprobar(UsuarioActual usuario, Integer postulacionId);

    PostulacionResponse rechazar(UsuarioActual usuario, Integer postulacionId);

    UsoPlanResponse usoPlan(UsuarioActual pyme);

    ContextoPostulacionResponse contexto(Integer postulacionId);

    PaginaResponse<PostulacionResponse> adminListar(UsuarioActual admin, String estado, int page, int size);
}
