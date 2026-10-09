package cl.licitawatch.licitaciones.bff.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bff.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bff.dto.response.CatalogosResponse;
import cl.licitawatch.licitaciones.bff.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.UsoPlanResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/** Adaptación para el frontend (sin reglas de negocio). */
public interface LicitacionBffService {
    PaginaResponse<LicitacionResponse> buscar(String q, Integer rubroId, Integer regionId, BigDecimal presupuestoMin,
                                              BigDecimal presupuestoMax, String orden, int page, int size);

    List<LicitacionResponse> mias();

    CatalogosResponse catalogos();

    LicitacionResponse obtener(Integer id);

    LicitacionResponse crear(LicitacionRequest request);

    LicitacionResponse actualizar(Integer id, LicitacionRequest request);

    LicitacionResponse cerrar(Integer id);

    void eliminar(Integer id);

    LicitacionResponse subirImagen(Integer id, MultipartFile archivo);

    LicitacionResponse quitarImagen(Integer id);

    LicitacionResponse subirDocumento(Integer id, MultipartFile archivo);

    LicitacionResponse quitarDocumento(Integer id);

    ResponseEntity<byte[]> archivo(Integer licitacionId, String nombre);

    PostulacionResponse postular(Integer licitacionId, PostularRequest request);

    List<PostulacionResponse> postulantes(Integer licitacionId);

    List<PostulacionResponse> misPostulaciones();

    UsoPlanResponse uso();

    PostulacionResponse aprobar(Integer postulacionId);

    PostulacionResponse rechazar(Integer postulacionId);

    PaginaResponse<LicitacionResponse> adminListar(String q, String estado, Integer rubroId, Integer regionId, int page, int size);

    LicitacionResponse adminEstado(Integer id, String estado);

    PaginaResponse<PostulacionResponse> adminPostulaciones(String estado, int page, int size);
}
