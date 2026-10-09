package cl.licitawatch.licitaciones.bs.mapper;

import cl.licitawatch.licitaciones.bs.client.dto.LicitacionBdDto;
import cl.licitawatch.licitaciones.bs.client.dto.PerfilDto;
import cl.licitawatch.licitaciones.bs.client.dto.PostulacionBdDto;
import cl.licitawatch.licitaciones.bs.dto.response.LicitacionResponse;
import cl.licitawatch.licitaciones.bs.dto.response.PostulacionResponse;
import cl.licitawatch.licitaciones.bs.util.Estados;
import cl.licitawatch.licitaciones.bs.util.Fechas;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class LicitacionMapper {

    public LicitacionResponse licitacion(LicitacionBdDto l, String licitadorNombre, String rubroNombre, String regionNombre,
                                         PostulacionBdDto miPostulacion) {
        Integer cupos = l.maxPostulantes() == null ? null : (int) Math.max(0, l.maxPostulantes() - l.cantidadPostulaciones());
        boolean disponible = Estados.ABIERTA.equals(l.estado()) && !l.fechaCierre().isBefore(Fechas.hoy())
                && (cupos == null || cupos > 0);
        return LicitacionResponse.builder().id(l.id()).licitadorId(l.licitadorId()).licitadorNombre(licitadorNombre)
                .titulo(l.titulo()).descripcion(l.descripcion()).rubroId(l.rubroId()).rubroNombre(rubroNombre)
                .regionId(l.regionId()).regionNombre(regionNombre).presupuestoMin(l.presupuestoMin()).presupuestoMax(l.presupuestoMax())
                .maxPostulantes(l.maxPostulantes()).imagenUrl(l.imagenUrl()).archivoUrl(l.archivoUrl()).archivoNombre(l.archivoNombre())
                .tipoArchivo(l.tipoArchivo()).fechaCierre(l.fechaCierre()).estado(l.estado()).createdAt(l.createdAt())
                .cantidadPostulaciones(l.cantidadPostulaciones()).cuposDisponibles(cupos)
                .diasParaCierre(ChronoUnit.DAYS.between(Fechas.hoy(), l.fechaCierre())).disponibleParaPostular(disponible)
                .miPostulacionId(miPostulacion != null ? miPostulacion.id() : null)
                .miPostulacionEstado(miPostulacion != null ? miPostulacion.estado() : null).build();
    }

    public PostulacionResponse postulacion(PostulacionBdDto p, PerfilDto pyme, String licitadorNombre) {
        PostulacionResponse.PostulacionResponseBuilder b = PostulacionResponse.builder().id(p.id()).licitacionId(p.licitacionId())
                .licitacionTitulo(p.licitacionTitulo()).licitacionEstado(p.licitacionEstado()).fechaCierre(p.fechaCierre())
                .licitadorId(p.licitadorId()).licitadorNombre(licitadorNombre).pymeId(p.pymeId()).mensaje(p.mensaje())
                .fechaPostulacion(p.fechaPostulacion()).estado(p.estado()).updatedAt(p.updatedAt())
                .chatDisponible(Estados.APROBADA.equals(p.estado()));
        if (pyme != null) {
            b.pymeUsuarioId(pyme.usuarioId()).pymeRazonSocial(pyme.razonSocial()).pymeRut(pyme.rut()).pymeRubro(pyme.rubroNombre())
                    .pymeCiudad(pyme.ciudadNombre()).pymeRegion(pyme.regionNombre()).pymeTamano(pyme.tamanoEmpresaNombre())
                    .pymeContacto(pyme.nombreContacto()).pymeEmailContacto(pyme.emailContacto()).pymeTelefono(pyme.telefono())
                    .pymePremium(pyme.premium());
        }
        return b.build();
    }
}
