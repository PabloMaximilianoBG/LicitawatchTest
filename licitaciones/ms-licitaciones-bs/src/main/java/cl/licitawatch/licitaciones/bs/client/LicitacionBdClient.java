package cl.licitawatch.licitaciones.bs.client;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.licitaciones.bs.client.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Contrato REST de MS.licitaciones.bd. */
@HttpExchange("/bd")
public interface LicitacionBdClient {

    @GetExchange("/catalogos")
    CatalogosBdDto catalogos();

    @GetExchange("/licitaciones")
    PaginaResponse<LicitacionBdDto> buscar(@RequestParam(required = false) String q, @RequestParam(required = false) Integer rubroId,
                                           @RequestParam(required = false) Integer regionId, @RequestParam(required = false) String estado,
                                           @RequestParam(required = false) Integer licitadorId,
                                           @RequestParam(required = false) BigDecimal presupuestoMin,
                                           @RequestParam(required = false) BigDecimal presupuestoMax,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate vigenteDesde,
                                           @RequestParam(required = false) Boolean conCupo, @RequestParam(required = false) String orden,
                                           @RequestParam int page, @RequestParam int size);

    @GetExchange("/licitaciones/{id}")
    LicitacionBdDto obtener(@PathVariable Integer id);

    @GetExchange("/licitaciones/por-ids")
    List<LicitacionBdDto> porIds(@RequestParam List<Integer> ids);

    @GetExchange("/licitaciones/vencidas")
    List<LicitacionBdDto> vencidas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha);

    @PostExchange("/licitaciones")
    LicitacionBdDto crear(@RequestBody LicitacionBdRequestDto request);

    @PutExchange("/licitaciones/{id}")
    LicitacionBdDto actualizar(@PathVariable Integer id, @RequestBody LicitacionBdRequestDto request);

    @PatchExchange("/licitaciones/{id}/estado")
    LicitacionBdDto cambiarEstado(@PathVariable Integer id, @RequestBody EstadoDto request);

    @PatchExchange("/licitaciones/{id}/archivos")
    LicitacionBdDto actualizarArchivos(@PathVariable Integer id, @RequestBody ArchivosBdDto request);

    @DeleteExchange("/licitaciones/{id}")
    EliminacionBdDto eliminar(@PathVariable Integer id);

    @GetExchange("/postulaciones")
    PaginaResponse<PostulacionBdDto> postulaciones(@RequestParam(required = false) Integer licitacionId,
                                                   @RequestParam(required = false) Integer pymeId,
                                                   @RequestParam(required = false) String estado,
                                                   @RequestParam int page, @RequestParam int size);

    @GetExchange("/licitaciones/{id}/postulaciones")
    List<PostulacionBdDto> postulacionesDeLicitacion(@PathVariable Integer id);

    @GetExchange("/postulaciones/{id}")
    PostulacionBdDto postulacion(@PathVariable Integer id);

    @PostExchange("/postulaciones")
    PostulacionBdDto crearPostulacion(@RequestBody PostulacionBdRequestDto request);

    @PatchExchange("/postulaciones/{id}/estado")
    PostulacionBdDto cambiarEstadoPostulacion(@PathVariable Integer id, @RequestBody EstadoDto request);

    @GetExchange("/postulaciones/contar")
    ConteoDto contarPostulaciones(@RequestParam Integer pymeId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde);

    @GetExchange("/postulaciones/existe")
    Boolean existePostulacion(@RequestParam Integer licitacionId, @RequestParam Integer pymeId);

    @PostExchange("/licitaciones/{id}/adjudicar")
    AdjudicacionBdDto adjudicar(@PathVariable Integer id, @RequestBody AdjudicarDto request);
}
