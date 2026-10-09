package cl.licitawatch.usuarios.bd.mapper;

import cl.licitawatch.usuarios.bd.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bd.dto.response.CiudadResponse;
import cl.licitawatch.usuarios.bd.dto.response.PerfilBdResponse;
import cl.licitawatch.usuarios.bd.dto.response.UsuarioBdResponse;
import cl.licitawatch.usuarios.bd.entity.*;
import org.springframework.stereotype.Component;

/** Conversión entidad -> DTO (las entidades nunca salen de esta capa). */
@Component
public class UsuarioBdMapper {

    public UsuarioBdResponse usuario(Usuario u) {
        return new UsuarioBdResponse(u.getId(), u.getEmail(), u.getPassword(), u.getRol().getNombre(), u.getActivo(), u.getCreatedAt());
    }

    public CatalogoResponse rol(Rol r) {
        return new CatalogoResponse(r.getId(), r.getNombre());
    }

    public CatalogoResponse rubro(Rubro r) {
        return new CatalogoResponse(r.getId(), r.getNombre());
    }

    public CatalogoResponse region(Region r) {
        return new CatalogoResponse(r.getId(), r.getNombre());
    }

    public CatalogoResponse tamano(TamanoEmpresa t) {
        return new CatalogoResponse(t.getId(), t.getNombre());
    }

    public CiudadResponse ciudad(Ciudad c) {
        return new CiudadResponse(c.getId(), c.getNombre(), c.getRegion().getId(), c.getRegion().getNombre());
    }

    public PerfilBdResponse soloUsuario(Usuario u) {
        return base(u).build();
    }

    public PerfilBdResponse licitador(Licitador l) {
        return base(l.getUsuario()).perfilId(l.getId()).razonSocial(l.getRazonSocial()).rut(l.getRut())
                .nombreContacto(l.getNombreContacto()).emailContacto(l.getEmailContacto()).telefono(l.getTelefono())
                .rubroId(l.getRubro().getId()).rubroNombre(l.getRubro().getNombre())
                .ciudadId(l.getCiudad().getId()).ciudadNombre(l.getCiudad().getNombre())
                .regionId(l.getCiudad().getRegion().getId()).regionNombre(l.getCiudad().getRegion().getNombre())
                .descripcionEmpresa(l.getDescripcionEmpresa()).sitioWeb(l.getSitioWeb()).updatedAt(l.getUpdatedAt())
                .build();
    }

    public PerfilBdResponse pyme(Pyme p) {
        return base(p.getUsuario()).perfilId(p.getId()).razonSocial(p.getRazonSocial()).rut(p.getRut())
                .nombreContacto(p.getNombreContacto()).emailContacto(p.getEmailContacto()).telefono(p.getTelefono())
                .rubroId(p.getRubro().getId()).rubroNombre(p.getRubro().getNombre())
                .ciudadId(p.getCiudad().getId()).ciudadNombre(p.getCiudad().getNombre())
                .regionId(p.getCiudad().getRegion().getId()).regionNombre(p.getCiudad().getRegion().getNombre())
                .tamanoEmpresaId(p.getTamanoEmpresa().getId()).tamanoEmpresaNombre(p.getTamanoEmpresa().getNombre())
                .descripcionEmpresa(p.getDescripcionEmpresa()).sitioWeb(p.getSitioWeb()).updatedAt(p.getUpdatedAt())
                .build();
    }

    public PerfilBdResponse administrador(Administrador a) {
        return base(a.getUsuario()).perfilId(a.getId()).nombre(a.getNombre()).area(a.getArea()).build();
    }

    private PerfilBdResponse.PerfilBdResponseBuilder base(Usuario u) {
        return PerfilBdResponse.builder().usuarioId(u.getId()).email(u.getEmail()).password(u.getPassword())
                .rolNombre(u.getRol().getNombre()).activo(u.getActivo()).createdAt(u.getCreatedAt());
    }
}
