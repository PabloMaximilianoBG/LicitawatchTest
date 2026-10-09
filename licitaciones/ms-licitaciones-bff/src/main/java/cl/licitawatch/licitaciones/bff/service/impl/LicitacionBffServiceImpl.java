package cl.licitawatch.licitaciones.bff.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.licitaciones.bff.client.LicitacionBsClient;
import cl.licitawatch.licitaciones.bff.dto.request.CambiarEstadoLicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.request.LicitacionRequest;
import cl.licitawatch.licitaciones.bff.dto.request.PostularRequest;
import cl.licitawatch.licitaciones.bff.dto.response.CatalogosResponse;
import cl.licitawatch.licitaciones.bff.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bff.dto.response.UsoPlanResponse;
import cl.licitawatch.licitaciones.bff.service.LicitacionBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LicitacionBffServiceImpl implements LicitacionBffService {
    private final LicitacionBsClient bs;

    @Override
    public PaginaResponse<LicitacionResponse> buscar(String q, Integer rubroId, Integer regionId, BigDecimal presupuestoMin,
                                                     BigDecimal presupuestoMax, String orden, int page, int size) {
        return bs.buscar(q, rubroId, regionId, presupuestoMin, presupuestoMax, orden, page, size);
    }

    @Override
    public List<LicitacionResponse> mias() {
        return bs.mias();
    }

    @Override
    public CatalogosResponse catalogos() {
        return bs.catalogos();
    }

    @Override
    public LicitacionResponse obtener(Integer id) {
        return bs.obtener(id);
    }

    @Override
    public LicitacionResponse crear(LicitacionRequest request) {
        return bs.crear(request);
    }

    @Override
    public LicitacionResponse actualizar(Integer id, LicitacionRequest request) {
        return bs.actualizar(id, request);
    }

    @Override
    public LicitacionResponse cerrar(Integer id) {
        return bs.cerrar(id);
    }

    @Override
    public void eliminar(Integer id) {
        bs.eliminar(id);
    }

    @Override
    public LicitacionResponse subirImagen(Integer id, MultipartFile archivo) {
        return bs.subirImagen(id, partes(archivo));
    }

    @Override
    public LicitacionResponse quitarImagen(Integer id) {
        return bs.quitarImagen(id);
    }

    @Override
    public LicitacionResponse subirDocumento(Integer id, MultipartFile archivo) {
        return bs.subirDocumento(id, partes(archivo));
    }

    @Override
    public LicitacionResponse quitarDocumento(Integer id) {
        return bs.quitarDocumento(id);
    }

    @Override
    public ResponseEntity<byte[]> archivo(Integer licitacionId, String nombre) {
        ResponseEntity<byte[]> r = bs.archivo(licitacionId, nombre);
        HttpHeaders h = new HttpHeaders();
        for (String cabecera : List.of(HttpHeaders.CONTENT_TYPE, HttpHeaders.CONTENT_DISPOSITION, HttpHeaders.CACHE_CONTROL, "X-Content-Type-Options")) {
            List<String> valores = r.getHeaders().get(cabecera);
            if (valores != null) {
                h.put(cabecera, valores);
            }
        }
        return ResponseEntity.ok().headers(h).body(r.getBody());
    }

    @Override
    public PostulacionResponse postular(Integer licitacionId, PostularRequest request) {
        return bs.postular(licitacionId, request);
    }

    @Override
    public List<PostulacionResponse> postulantes(Integer licitacionId) {
        return bs.postulantes(licitacionId);
    }

    @Override
    public List<PostulacionResponse> misPostulaciones() {
        return bs.misPostulaciones();
    }

    @Override
    public UsoPlanResponse uso() {
        return bs.uso();
    }

    @Override
    public PostulacionResponse aprobar(Integer postulacionId) {
        return bs.aprobar(postulacionId);
    }

    @Override
    public PostulacionResponse rechazar(Integer postulacionId) {
        return bs.rechazar(postulacionId);
    }

    @Override
    public PaginaResponse<LicitacionResponse> adminListar(String q, String estado, Integer rubroId, Integer regionId, int page, int size) {
        return bs.adminListar(q, estado, rubroId, regionId, page, size);
    }

    @Override
    public LicitacionResponse adminEstado(Integer id, String estado) {
        return bs.adminEstado(id, new CambiarEstadoLicitacionRequest(estado));
    }

    @Override
    public PaginaResponse<PostulacionResponse> adminPostulaciones(String estado, int page, int size) {
        return bs.adminPostulaciones(estado, page, size);
    }

    /** Reenvía el archivo recibido del frontend a MS.licitaciones.bs como multipart. */
    private static MultiValueMap<String, HttpEntity<?>> partes(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("ARCHIVO_VACIO", "Selecciona un archivo");
        }
        try {
            String nombre = archivo.getOriginalFilename();
            ByteArrayResource recurso = new ByteArrayResource(archivo.getBytes()) {
                @Override
                public String getFilename() {
                    return nombre;
                }
            };
            HttpHeaders cabeceras = new HttpHeaders();
            cabeceras.setContentDisposition(ContentDisposition.formData().name("archivo").filename(nombre, StandardCharsets.UTF_8).build());
            cabeceras.setContentType(archivo.getContentType() != null
                    ? MediaType.parseMediaType(archivo.getContentType()) : MediaType.APPLICATION_OCTET_STREAM);
            MultiValueMap<String, HttpEntity<?>> partes = new LinkedMultiValueMap<>();
            partes.add("archivo", new HttpEntity<>(recurso, cabeceras));
            return partes;
        } catch (IOException e) {
            throw new BadRequestException("ARCHIVO_INVALIDO", "No se pudo leer el archivo");
        }
    }
}
