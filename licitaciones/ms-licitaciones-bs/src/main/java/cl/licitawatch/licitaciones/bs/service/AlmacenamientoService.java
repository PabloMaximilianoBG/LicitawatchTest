package cl.licitawatch.licitaciones.bs.service;

import cl.licitawatch.licitaciones.bs.dto.response.ArchivoDescarga;
import org.springframework.web.multipart.MultipartFile;

/** Guarda la imagen y el documento de una licitación. En la BD solo queda la URL generada (ER pág. 3). */
public interface AlmacenamientoService {
    record Guardado(String nombreGuardado, String nombreOriginal, String tipoArchivo, String url) {
    }

    Guardado guardarImagen(Integer licitacionId, MultipartFile archivo);

    Guardado guardarDocumento(Integer licitacionId, MultipartFile archivo);

    ArchivoDescarga leer(Integer licitacionId, String nombreGuardado);

    void eliminarPorUrl(Integer licitacionId, String url);

    void eliminarTodo(Integer licitacionId);
}
