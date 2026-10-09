package cl.licitawatch.licitaciones.bff.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bff.dto.request.CambiarEstadoLicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bff.dto.response.CatalogosResponse;
import cl.licitawatch.licitaciones.bff.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.UsoPlanResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/** Contrato REST de MS.licitaciones.bs. */
@HttpExchange("/bs")
public interface LicitacionBsClient {

    @GetExchange("/licitaciones")
    PaginaResponse<LicitacionResponse> buscar(@RequestParam(required = false) String q, @RequestParam(required = false) Integer rubroId,
                                              @RequestParam(required = false) Integer regionId,
                                              @RequestParam(required = false) BigDecimal presupuestoMin,
                                              @RequestParam(required = false) BigDecimal presupuestoMax,
                                              @RequestParam(required = false) String orden, @RequestParam int page, @RequestParam int size);

    @GetExchange("/licitaciones/mias")
    List<LicitacionResponse> mias();

    @GetExchange("/licitaciones/catalogos")
    CatalogosResponse catalogos();

    @GetExchange("/licitaciones/{id}")
    LicitacionResponse obtener(@PathVariable Integer id);

    @PostExchange("/licitaciones")
    LicitacionResponse crear(@RequestBody LicitacionRequest request);

    @PutExchange("/licitaciones/{id}")
    LicitacionResponse actualizar(@PathVariable Integer id, @RequestBody LicitacionRequest request);

    @PatchExchange("/licitaciones/{id}/cerrar")
    LicitacionResponse cerrar(@PathVariable Integer id);

    @DeleteExchange("/licitaciones/{id}")
    void eliminar(@PathVariable Integer id);

    @PostExchange(value = "/licitaciones/{id}/imagen", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    LicitacionResponse subirImagen(@PathVariable Integer id, @RequestBody MultiValueMap<String, HttpEntity<?>> partes);

    @DeleteExchange("/licitaciones/{id}/imagen")
    LicitacionResponse quitarImagen(@PathVariable Integer id);

    @PostExchange(value = "/licitaciones/{id}/archivo", contentType = MediaType.MULTIPART_FORM_DATA_VALUE)
    LicitacionResponse subirDocumento(@PathVariable Integer id, @RequestBody MultiValueMap<String, HttpEntity<?>> partes);

    @DeleteExchange("/licitaciones/{id}/archivo")
    LicitacionResponse quitarDocumento(@PathVariable Integer id);

    @GetExchange("/archivos/{licitacionId}/{nombre}")
    ResponseEntity<byte[]> archivo(@PathVariable Integer licitacionId, @PathVariable String nombre);

    @PostExchange("/licitaciones/{id}/postulaciones")
    PostulacionResponse postular(@PathVariable Integer id, @RequestBody PostularRequest request);

    @GetExchange("/licitaciones/{id}/postulaciones")
    List<PostulacionResponse> postulantes(@PathVariable Integer id);

    @GetExchange("/postulaciones/mias")
    List<PostulacionResponse> misPostulaciones();

    @GetExchange("/postulaciones/uso")
    UsoPlanResponse uso();

    @PatchExchange("/postulaciones/{id}/aprobar")
    PostulacionResponse aprobar(@PathVariable Integer id);

    @PatchExchange("/postulaciones/{id}/rechazar")
    PostulacionResponse rechazar(@PathVariable Integer id);

    @GetExchange("/admin/licitaciones")
    PaginaResponse<LicitacionResponse> adminListar(@RequestParam(required = false) String q, @RequestParam(required = false) String estado,
                                                   @RequestParam(required = false) Integer rubroId,
                                                   @RequestParam(required = false) Integer regionId,
                                                   @RequestParam int page, @RequestParam int size);

    @PatchExchange("/admin/licitaciones/{id}/estado")
    LicitacionResponse adminEstado(@PathVariable Integer id, @RequestBody CambiarEstadoLicitacionRequest request);

    @GetExchange("/admin/postulaciones")
    PaginaResponse<PostulacionResponse> adminPostulaciones(@RequestParam(required = false) String estado,
                                                           @RequestParam int page, @RequestParam int size);
}
