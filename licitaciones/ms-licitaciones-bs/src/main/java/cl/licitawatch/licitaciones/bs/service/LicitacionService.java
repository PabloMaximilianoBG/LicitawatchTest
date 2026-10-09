package cl.licitawatch.licitaciones.bs.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.licitaciones.bs.dto.request.BusquedaRequest;
import cl.licitawatch.licitaciones.bs.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bs.dto.response.ArchivoDescarga;
import cl.licitawatch.licitaciones.bs.dto.response.CatalogosResponse;
import cl.licitawatch.licitaciones.bs.dto.response.LicitacionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** PPT diap. 6: el Licitador publica, edita/cierra y administra sus licitaciones; la Pyme busca y filtra. */
public interface LicitacionService {
    PaginaResponse<LicitacionResponse> buscar(UsuarioActual usuario, BusquedaRequest filtros);

    List<LicitacionResponse> misLicitaciones(UsuarioActual usuario);

    LicitacionResponse obtener(UsuarioActual usuario, Integer id);

    LicitacionResponse crear(UsuarioActual usuario, LicitacionRequest request);

    LicitacionResponse actualizar(UsuarioActual usuario, Integer id, LicitacionRequest request);

    LicitacionResponse cerrar(UsuarioActual usuario, Integer id);

    void eliminar(UsuarioActual usuario, Integer id);

    LicitacionResponse subirImagen(UsuarioActual usuario, Integer id, MultipartFile archivo);

    LicitacionResponse subirDocumento(UsuarioActual usuario, Integer id, MultipartFile archivo);

    LicitacionResponse quitarImagen(UsuarioActual usuario, Integer id);

    LicitacionResponse quitarDocumento(UsuarioActual usuario, Integer id);

    ArchivoDescarga archivo(Integer licitacionId, String nombre);

    CatalogosResponse catalogos();

    PaginaResponse<LicitacionResponse> adminListar(UsuarioActual admin, BusquedaRequest filtros);

    LicitacionResponse adminCambiarEstado(UsuarioActual admin, Integer id, String estado);
}
