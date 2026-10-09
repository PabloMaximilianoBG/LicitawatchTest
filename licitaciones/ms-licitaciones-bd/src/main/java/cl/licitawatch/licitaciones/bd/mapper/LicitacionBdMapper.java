package cl.licitawatch.licitaciones.bd.mapper;

import cl.licitawatch.licitaciones.bd.dto.response.LicitacionBdResponse;
import cl.licitawatch.licitaciones.bd.dto.response.PostulacionBdResponse;
import cl.licitawatch.licitaciones.bd.entity.Licitacion;
import cl.licitawatch.licitaciones.bd.entity.Postulacion;
import org.springframework.stereotype.Component;

@Component
public class LicitacionBdMapper {

    public LicitacionBdResponse licitacion(Licitacion l, long cantidadPostulaciones) {
        return LicitacionBdResponse.builder().id(l.getId()).licitadorId(l.getLicitadorId()).titulo(l.getTitulo())
                .descripcion(l.getDescripcion()).rubroId(l.getRubroId()).regionId(l.getRegionId())
                .presupuestoMin(l.getPresupuestoMin()).presupuestoMax(l.getPresupuestoMax()).maxPostulantes(l.getMaxPostulantes())
                .imagenUrl(l.getImagenUrl()).archivoUrl(l.getArchivoUrl()).archivoNombre(l.getArchivoNombre())
                .tipoArchivo(l.getTipoArchivo() != null ? l.getTipoArchivo().getNombre() : null)
                .fechaCierre(l.getFechaCierre()).estado(l.getEstadoLicitacion().getNombre()).createdAt(l.getCreatedAt())
                .cantidadPostulaciones(cantidadPostulaciones).build();
    }

    public PostulacionBdResponse postulacion(Postulacion p) {
        Licitacion l = p.getLicitacion();
        return PostulacionBdResponse.builder().id(p.getId()).licitacionId(l.getId()).licitacionTitulo(l.getTitulo())
                .licitadorId(l.getLicitadorId()).licitacionEstado(l.getEstadoLicitacion().getNombre()).fechaCierre(l.getFechaCierre())
                .pymeId(p.getPymeId()).mensaje(p.getMensaje()).fechaPostulacion(p.getFechaPostulacion())
                .estado(p.getEstadoPostulacion().getNombre()).updatedAt(p.getUpdatedAt()).build();
    }
}
