package cl.licitawatch.usuarios.bs.service.impl;

import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.seguridad.Roles;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.usuarios.bs.client.UsuarioBdClient;
import cl.licitawatch.usuarios.bs.client.VentasClient;
import cl.licitawatch.usuarios.bs.client.dto.ActualizarPerfilBdDto;
import cl.licitawatch.usuarios.bs.client.dto.DatosAdministradorBdDto;
import cl.licitawatch.usuarios.bs.client.dto.DatosEmpresaBdDto;
import cl.licitawatch.usuarios.bs.client.dto.PerfilBdDto;
import cl.licitawatch.usuarios.bs.dto.request.ActualizarPerfilRequest;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bs.dto.response.UsuarioBasicoResponse;
import cl.licitawatch.usuarios.bs.mapper.PerfilMapper;
import cl.licitawatch.usuarios.bs.service.PerfilService;
import cl.licitawatch.usuarios.bs.util.RolesBd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/** Consulta y edición de perfiles (PPT: "Licitadores y Pymes actualizan sus datos"; el RUT y el correo de acceso no se editan). */
@Slf4j
@Service
@RequiredArgsConstructor
public class PerfilServiceImpl implements PerfilService {
    private final UsuarioBdClient bd;
    private final VentasClient ventas;
    private final PerfilMapper mapper;

    @Override
    public PerfilResponse miPerfil(UsuarioActual u) {
        return conPremium(List.of(bd.perfil(u.usuarioId()))).get(0);
    }

    @Override
    public PerfilResponse actualizarMiPerfil(UsuarioActual u, ActualizarPerfilRequest r) {
        return actualizarPerfil(u.usuarioId(), r);
    }

    @Override
    public PerfilResponse actualizarPerfil(Integer usuarioId, ActualizarPerfilRequest r) {
        PerfilBdDto actual = bd.perfil(usuarioId);
        String rol = RolesBd.aCodigo(actual.rolNombre());
        ActualizarPerfilBdDto cambios;
        if (Roles.ADMINISTRADOR.equals(rol)) {
            Map<String, String> errores = new LinkedHashMap<>();
            requerido(errores, "nombre", r.nombre());
            requerido(errores, "area", r.area());
            lanzarSiHay(errores);
            cambios = new ActualizarPerfilBdDto(null, new DatosAdministradorBdDto(r.nombre().trim(), r.area().trim()));
        } else {
            cambios = new ActualizarPerfilBdDto(datosEmpresa(r, Roles.PYME.equals(rol)), null);
        }
        return conPremium(List.of(bd.actualizarPerfil(usuarioId, cambios))).get(0);
    }

    @Override
    public UsuarioBasicoResponse usuarioBasico(Integer usuarioId) {
        return mapper.basico(bd.perfil(usuarioId));
    }

    @Override
    public PerfilResponse licitador(Integer licitadorId) {
        return mapper.perfil(bd.licitador(licitadorId), false);
    }

    @Override
    public List<PerfilResponse> licitadores(List<Integer> ids) {
        return ids.isEmpty() ? List.of() : bd.licitadores(ids).stream().map(p -> mapper.perfil(p, false)).toList();
    }

    @Override
    public PerfilResponse pyme(Integer pymeId) {
        return conPremium(List.of(bd.pyme(pymeId))).get(0);
    }

    @Override
    public List<PerfilResponse> pymes(List<Integer> ids) {
        return ids.isEmpty() ? List.of() : conPremium(bd.pymes(ids));
    }

    @Override
    public List<PerfilResponse> pymesPorRubro(Integer rubroId) {
        return conPremium(bd.pymesPorRubro(rubroId));
    }

    /** Agrega la insignia Premium (PPT diap. 7) consultando a MS.ventas.bs en un solo llamado. */
    private List<PerfilResponse> conPremium(List<PerfilBdDto> perfiles) {
        List<Integer> pymes = perfiles.stream().filter(p -> "Pyme".equalsIgnoreCase(p.rolNombre())).map(PerfilBdDto::usuarioId).toList();
        Set<Integer> premium = new HashSet<>();
        if (!pymes.isEmpty()) {
            try {
                premium.addAll(ventas.usuariosPremium(pymes));
            } catch (Exception e) {
                log.warn("No se pudo consultar la insignia Premium: {}", e.getMessage());
            }
        }
        return perfiles.stream().map(p -> mapper.perfil(p, premium.contains(p.usuarioId()))).toList();
    }

    private static DatosEmpresaBdDto datosEmpresa(ActualizarPerfilRequest r, boolean esPyme) {
        Map<String, String> errores = new LinkedHashMap<>();
        requerido(errores, "razonSocial", r.razonSocial());
        requerido(errores, "nombreContacto", r.nombreContacto());
        requerido(errores, "emailContacto", r.emailContacto());
        requerido(errores, "telefono", r.telefono());
        requerido(errores, "descripcionEmpresa", r.descripcionEmpresa());
        if (r.rubroId() == null) {
            errores.put("rubroId", "Selecciona un rubro");
        }
        if (r.ciudadId() == null) {
            errores.put("ciudadId", "Selecciona una ciudad");
        }
        if (esPyme && r.tamanoEmpresaId() == null) {
            errores.put("tamanoEmpresaId", "Selecciona el tamaño de la empresa");
        }
        lanzarSiHay(errores);
        return DatosEmpresaBdDto.builder().razonSocial(r.razonSocial().trim()).nombreContacto(r.nombreContacto())
                .emailContacto(r.emailContacto()).telefono(r.telefono()).rubroId(r.rubroId()).ciudadId(r.ciudadId())
                .tamanoEmpresaId(esPyme ? r.tamanoEmpresaId() : null).descripcionEmpresa(r.descripcionEmpresa())
                .sitioWeb(r.sitioWeb()).build();
    }

    private static void requerido(Map<String, String> errores, String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            errores.put(campo, "Este campo es obligatorio");
        }
    }

    private static void lanzarSiHay(Map<String, String> errores) {
        if (!errores.isEmpty()) {
            throw new CamposInvalidosException(errores);
        }
    }

    /** 400 con el detalle por campo. */
    static class CamposInvalidosException extends BadRequestException {
        private final transient Map<String, String> errores;

        CamposInvalidosException(Map<String, String> errores) {
            super("VALIDACION", "Faltan campos obligatorios: " + String.join(", ", errores.keySet()));
            this.errores = errores;
        }

        Map<String, String> errores() {
            return errores;
        }
    }
}
