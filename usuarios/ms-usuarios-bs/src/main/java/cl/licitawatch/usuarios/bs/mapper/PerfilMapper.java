package cl.licitawatch.usuarios.bs.mapper;

import cl.licitawatch.usuarios.bs.client.dto.CatalogoDto;
import cl.licitawatch.usuarios.bs.client.dto.CiudadDto;
import cl.licitawatch.usuarios.bs.client.dto.PerfilBdDto;
import cl.licitawatch.usuarios.bs.dto.response.CatalogoResponse;
import cl.licitawatch.usuarios.bs.dto.response.CiudadResponse;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bs.dto.response.UsuarioBasicoResponse;
import cl.licitawatch.usuarios.bs.util.EstadoCuenta;
import cl.licitawatch.usuarios.bs.util.RolesBd;
import org.springframework.stereotype.Component;

/** Convierte las respuestas de MS.usuarios.bd en DTOs de negocio (nunca expone el hash de la contraseña). */
@Component
public class PerfilMapper {

    public PerfilResponse perfil(PerfilBdDto p, boolean premium) {
        return PerfilResponse.builder()
                .usuarioId(p.usuarioId()).email(p.email()).rol(RolesBd.aCodigo(p.rolNombre())).activo(p.activo())
                .estadoCuenta(EstadoCuenta.de(p.activo(), p.password()).name()).createdAt(p.createdAt())
                .perfilId(p.perfilId()).razonSocial(p.razonSocial()).rut(p.rut()).nombreContacto(p.nombreContacto())
                .emailContacto(p.emailContacto()).telefono(p.telefono()).rubroId(p.rubroId()).rubroNombre(p.rubroNombre())
                .ciudadId(p.ciudadId()).ciudadNombre(p.ciudadNombre()).regionId(p.regionId()).regionNombre(p.regionNombre())
                .tamanoEmpresaId(p.tamanoEmpresaId()).tamanoEmpresaNombre(p.tamanoEmpresaNombre())
                .descripcionEmpresa(p.descripcionEmpresa()).sitioWeb(p.sitioWeb()).updatedAt(p.updatedAt())
                .nombre(p.nombre()).area(p.area()).premium(premium)
                .build();
    }

    public UsuarioBasicoResponse basico(PerfilBdDto p) {
        String nombre = p.razonSocial() != null ? p.razonSocial() : p.nombre() != null ? p.nombre() : p.email();
        return new UsuarioBasicoResponse(p.usuarioId(), p.email(), RolesBd.aCodigo(p.rolNombre()), p.activo(), nombre, p.perfilId());
    }

    public CatalogoResponse catalogo(CatalogoDto c) {
        return new CatalogoResponse(c.id(), c.nombre());
    }

    public CiudadResponse ciudad(CiudadDto c) {
        return new CiudadResponse(c.id(), c.nombre(), c.regionId(), c.regionNombre());
    }
}
