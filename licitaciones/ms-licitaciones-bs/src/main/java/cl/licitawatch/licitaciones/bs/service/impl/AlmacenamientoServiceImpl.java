package cl.licitawatch.licitaciones.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import cl.licitawatch.licitaciones.bs.dto.response.ArchivoDescarga;
import cl.licitawatch.licitaciones.bs.service.AlmacenamientoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Almacenamiento en disco local (on-premise): STORAGE_DIR/licitaciones/{id}/{imagen|documento}-{uuid}.{ext}. */
@Slf4j
@Service
public class AlmacenamientoServiceImpl implements AlmacenamientoService {
    private static final long MAX_IMAGEN = 5L * 1024 * 1024;
    private static final long MAX_DOCUMENTO = 10L * 1024 * 1024;
    private static final Map<String, String> IMAGENES = Map.of("jpg", "image/jpeg", "jpeg", "image/jpeg", "png", "image/png",
            "webp", "image/webp", "gif", "image/gif");
    private static final Set<String> PROHIBIDAS = Set.of("exe", "bat", "cmd", "com", "msi", "sh", "ps1", "js", "vbs", "jar",
            "html", "htm", "svg", "php", "dll", "scr");
    private static final Pattern NOMBRE_SEGURO = Pattern.compile("^(imagen|documento)-[0-9a-f\\-]{36}\\.[a-z0-9]{1,5}$");

    private final Path raiz;
    private final String baseUrl;

    public AlmacenamientoServiceImpl(@Value("${licitawatch.storage.dir}") String dir,
                                     @Value("${licitawatch.public-base-url}") String baseUrl) {
        this.raiz = Path.of(dir, "licitaciones").toAbsolutePath().normalize();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public Guardado guardarImagen(Integer licitacionId, MultipartFile archivo) {
        String ext = extension(archivo);
        if (!IMAGENES.containsKey(ext)) {
            throw new BadRequestException("IMAGEN_INVALIDA", "La imagen debe ser JPG, PNG, WEBP o GIF");
        }
        if (archivo.getSize() > MAX_IMAGEN) {
            throw new BadRequestException("ARCHIVO_DEMASIADO_GRANDE", "La imagen no puede superar 5 MB");
        }
        return guardar(licitacionId, archivo, "imagen", ext, null);
    }

    @Override
    public Guardado guardarDocumento(Integer licitacionId, MultipartFile archivo) {
        String ext = extension(archivo);
        if (PROHIBIDAS.contains(ext)) {
            throw new BadRequestException("ARCHIVO_INVALIDO", "Tipo de archivo no permitido");
        }
        if (archivo.getSize() > MAX_DOCUMENTO) {
            throw new BadRequestException("ARCHIVO_DEMASIADO_GRANDE", "El documento no puede superar 10 MB");
        }
        String tipo = switch (ext) {
            case "pdf" -> "PDF";
            case "docx" -> "DOCX";
            case "xlsx" -> "XLSX";
            default -> "Otro";
        };
        return guardar(licitacionId, archivo, "documento", ext, tipo);
    }

    @Override
    public ArchivoDescarga leer(Integer licitacionId, String nombre) {
        if (!NOMBRE_SEGURO.matcher(nombre).matches()) {
            throw new ResourceNotFoundException("ARCHIVO_NO_ENCONTRADO", "El archivo no existe");
        }
        Path p = carpeta(licitacionId).resolve(nombre).normalize();
        if (!p.startsWith(raiz) || !Files.isRegularFile(p)) {
            throw new ResourceNotFoundException("ARCHIVO_NO_ENCONTRADO", "El archivo no existe");
        }
        String ext = nombre.substring(nombre.lastIndexOf('.') + 1);
        String tipo = IMAGENES.getOrDefault(ext, switch (ext) {
            case "pdf" -> "application/pdf";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default -> "application/octet-stream";
        });
        try {
            return new ArchivoDescarga(Files.readAllBytes(p), tipo, nombre, nombre.startsWith("imagen-") || "pdf".equals(ext));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void eliminarPorUrl(Integer licitacionId, String url) {
        if (url == null || !url.contains("/")) {
            return;
        }
        String nombre = url.substring(url.lastIndexOf('/') + 1);
        if (!NOMBRE_SEGURO.matcher(nombre).matches()) {
            return;
        }
        try {
            Files.deleteIfExists(carpeta(licitacionId).resolve(nombre));
        } catch (IOException e) {
            log.warn("No se pudo eliminar {}: {}", nombre, e.getMessage());
        }
    }

    @Override
    public void eliminarTodo(Integer licitacionId) {
        try {
            FileSystemUtils.deleteRecursively(carpeta(licitacionId));
        } catch (IOException e) {
            log.warn("No se pudo eliminar la carpeta de la licitación {}: {}", licitacionId, e.getMessage());
        }
    }

    private Guardado guardar(Integer licitacionId, MultipartFile archivo, String prefijo, String ext, String tipo) {
        if (archivo.isEmpty()) {
            throw new BadRequestException("ARCHIVO_VACIO", "El archivo está vacío");
        }
        String nombre = prefijo + "-" + UUID.randomUUID() + "." + ext;
        try {
            Path dir = carpeta(licitacionId);
            Files.createDirectories(dir);
            archivo.transferTo(dir.resolve(nombre));
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar el archivo", e);
        }
        String original = archivo.getOriginalFilename() == null ? nombre : Path.of(archivo.getOriginalFilename()).getFileName().toString();
        if (original.length() > 255) {
            original = original.substring(original.length() - 255);
        }
        return new Guardado(nombre, original, tipo, baseUrl + "/api/licitaciones/archivos/" + licitacionId + "/" + nombre);
    }

    private Path carpeta(Integer licitacionId) {
        return raiz.resolve(String.valueOf(licitacionId));
    }

    private static String extension(MultipartFile archivo) {
        String n = archivo.getOriginalFilename();
        if (n == null || !n.contains(".")) {
            throw new BadRequestException("ARCHIVO_INVALIDO", "El archivo debe tener extensión");
        }
        return n.substring(n.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
