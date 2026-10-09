package cl.licitawatch.usuarios.bs.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.BusinessException;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.seguridad.Roles;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.usuarios.bs.client.UsuarioBdClient;
import cl.licitawatch.usuarios.bs.client.VentasClient;
import cl.licitawatch.usuarios.bs.client.dto.*;
import cl.licitawatch.usuarios.bs.dto.request.AdminCrearUsuarioRequest;
import cl.licitawatch.usuarios.bs.dto.request.CambiarEstadoRequest;
import cl.licitawatch.usuarios.bs.dto.request.CambiarRolRequest;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bs.mapper.PerfilMapper;
import cl.licitawatch.usuarios.bs.service.AdminUsuarioService;
import cl.licitawatch.usuarios.bs.util.EstadoCuenta;
import cl.licitawatch.usuarios.bs.util.RolesBd;
import cl.licitawatch.usuarios.bs.util.RutUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUsuarioServiceImpl implements AdminUsuarioService {
    private final UsuarioBdClient bd;
    private final VentasClient ventas;
    private final PasswordEncoder passwordEncoder;
    private final PerfilMapper mapper;

    @Value("${licitawatch.admin-inicial.email:}")
    private String adminEmail;
    @Value("${licitawatch.admin-inicial.password:}")
    private String adminPassword;
    @Value("${licitawatch.admin-inicial.nombre:Administrador}")
    private String adminNombre;
    @Value("${licitawatch.admin-inicial.area:Administracion}")
    private String adminArea;

    @Override
    public PaginaResponse<PerfilResponse> listar(String rol, Boolean activo, String q, int page, int size) {
        PaginaResponse<PerfilBdDto> pagina = bd.listar(RolesBd.aNombreBd(rol), activo, q, page, size);
        Set<Integer> premium = premium(pagina.content());
        return pagina.map(p -> mapper.perfil(p, premium.contains(p.usuarioId())));
    }

    @Override
    public PerfilResponse obtener(Integer usuarioId) {
        PerfilBdDto p = bd.perfil(usuarioId);
        return mapper.perfil(p, premium(List.of(p)).contains(usuarioId));
    }

    @Override
    public PerfilResponse crear(AdminCrearUsuarioRequest r) {
        AuthServiceImpl.validarPassword(r.password(), r.confirmPassword());
        String rolNombre = RolesBd.aNombreBd(r.rol());
        if (bd.existeEmail(r.email().trim()).existe()) {
            throw new ConflictException("EMAIL_DUPLICADO", "Ya existe una cuenta con ese correo");
        }
        DatosEmpresaBdDto empresa = null;
        DatosAdministradorBdDto admin = null;
        if ("Administrador".equals(rolNombre)) {
            admin = datosAdmin(r.nombre(), r.area());
        } else {
            empresa = datosEmpresa(rolNombre, r.razonSocial(), r.rut(), r.nombreContacto(), r.emailContacto(), r.telefono(),
                    r.rubroId(), r.ciudadId(), r.tamanoEmpresaId(), r.descripcionEmpresa(), r.sitioWeb());
        }
        PerfilBdDto creado = bd.crear(CrearUsuarioBdDto.builder().email(r.email()).password(passwordEncoder.encode(r.password()))
                .rolNombre(rolNombre).activo(true).empresa(empresa).administrador(admin).build());
        if ("Pyme".equals(rolNombre)) {
            asignarEstandar(creado.usuarioId());
        }
        return mapper.perfil(creado, false);
    }

    @Override
    public PerfilResponse cambiarEstado(UsuarioActual admin, Integer usuarioId, CambiarEstadoRequest r) {
        if (admin.usuarioId().equals(usuarioId)) {
            throw new BusinessException("OPERACION_NO_PERMITIDA", "No puedes cambiar el estado de tu propia cuenta");
        }
        UsuarioBdDto u = bd.obtener(usuarioId);
        String hash = Boolean.TRUE.equals(r.activo()) ? EstadoCuenta.desbloquear(u.password()) : EstadoCuenta.bloquear(u.password());
        bd.actualizar(usuarioId, ActualizarUsuarioBdDto.builder().activo(r.activo()).password(hash).build());
        return obtener(usuarioId);
    }

    @Override
    public PerfilResponse cambiarRol(UsuarioActual admin, Integer usuarioId, CambiarRolRequest r) {
        if (admin.usuarioId().equals(usuarioId)) {
            throw new BusinessException("OPERACION_NO_PERMITIDA", "No puedes cambiar tu propio rol");
        }
        String rolNombre = RolesBd.aNombreBd(r.rol());
        UsuarioBdDto u = bd.obtener(usuarioId);
        if (rolNombre.equalsIgnoreCase(u.rolNombre())) {
            return obtener(usuarioId);
        }
        PerfilesUsuarioDto perfiles = bd.perfiles(usuarioId);
        boolean tienePerfil = switch (rolNombre) {
            case "Licitador" -> perfiles.licitadorId() != null;
            case "Pyme" -> perfiles.pymeId() != null;
            default -> perfiles.administradorId() != null;
        };
        // Se conserva el perfil anterior: licitaciones, postulaciones y chats lo referencian (REF).
        if (!tienePerfil) {
            if ("Administrador".equals(rolNombre)) {
                bd.crearPerfil(usuarioId, new CrearPerfilBdDto(rolNombre, null, datosAdmin(r.nombre(), r.area())));
            } else {
                bd.crearPerfil(usuarioId, new CrearPerfilBdDto(rolNombre, datosEmpresa(rolNombre, r.razonSocial(), r.rut(),
                        r.nombreContacto(), r.emailContacto(), r.telefono(), r.rubroId(), r.ciudadId(), r.tamanoEmpresaId(),
                        r.descripcionEmpresa(), r.sitioWeb()), null));
            }
        }
        bd.actualizar(usuarioId, ActualizarUsuarioBdDto.builder().rolNombre(rolNombre).build());
        if ("Pyme".equals(rolNombre)) {
            asignarEstandar(usuarioId);
        }
        return obtener(usuarioId);
    }

    @Override
    public void asegurarAdministradorInicial() {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_EMAIL / ADMIN_PASSWORD no configurados: no se crea administrador inicial");
            return;
        }
        if (bd.existeEmail(adminEmail).existe()) {
            log.info("Administrador inicial {} ya existe", adminEmail);
            return;
        }
        bd.crear(CrearUsuarioBdDto.builder().email(adminEmail).password(passwordEncoder.encode(adminPassword))
                .rolNombre("Administrador").activo(true).administrador(new DatosAdministradorBdDto(adminNombre, adminArea)).build());
        log.info("Administrador inicial {} creado", adminEmail);
    }

    // ------------------------------------------------------------------ apoyo

    private Set<Integer> premium(List<PerfilBdDto> perfiles) {
        List<Integer> pymes = perfiles.stream().filter(p -> "Pyme".equalsIgnoreCase(p.rolNombre())).map(PerfilBdDto::usuarioId).toList();
        if (pymes.isEmpty()) {
            return Set.of();
        }
        try {
            return new HashSet<>(ventas.usuariosPremium(pymes));
        } catch (Exception e) {
            log.warn("No se pudo consultar planes Premium: {}", e.getMessage());
            return Set.of();
        }
    }

    private void asignarEstandar(Integer usuarioId) {
        try {
            ventas.asignarEstandar(new UsuarioIdDto(usuarioId));
        } catch (Exception e) {
            log.warn("No se pudo asignar el plan Estándar a {}: {}", usuarioId, e.getMessage());
        }
    }

    private static DatosAdministradorBdDto datosAdmin(String nombre, String area) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (nombre == null || nombre.isBlank()) {
            errores.put("nombre", "El nombre es obligatorio");
        }
        if (area == null || area.isBlank()) {
            errores.put("area", "El área es obligatoria");
        }
        if (!errores.isEmpty()) {
            throw new BadRequestException("VALIDACION", "Para el rol Administrador indica nombre y área");
        }
        return new DatosAdministradorBdDto(nombre.trim(), area.trim());
    }

    private static DatosEmpresaBdDto datosEmpresa(String rolNombre, String razonSocial, String rut, String nombreContacto,
                                                  String emailContacto, String telefono, Integer rubroId, Integer ciudadId,
                                                  Integer tamanoEmpresaId, String descripcion, String sitioWeb) {
        boolean pyme = "Pyme".equals(rolNombre);
        List<String> faltan = new ArrayList<>();
        if (razonSocial == null || razonSocial.isBlank()) faltan.add("razonSocial");
        if (rut == null || rut.isBlank()) faltan.add("rut");
        if (nombreContacto == null || nombreContacto.isBlank()) faltan.add("nombreContacto");
        if (emailContacto == null || emailContacto.isBlank()) faltan.add("emailContacto");
        if (telefono == null || telefono.isBlank()) faltan.add("telefono");
        if (rubroId == null) faltan.add("rubroId");
        if (ciudadId == null) faltan.add("ciudadId");
        if (pyme && tamanoEmpresaId == null) faltan.add("tamanoEmpresaId");
        if (descripcion == null || descripcion.isBlank()) faltan.add("descripcionEmpresa");
        if (!faltan.isEmpty()) {
            throw new BadRequestException("VALIDACION", "Para el rol " + rolNombre + " faltan: " + String.join(", ", faltan));
        }
        if (!RutUtils.esValido(rut)) {
            throw new BadRequestException("VALIDACION", "RUT inválido");
        }
        return DatosEmpresaBdDto.builder().razonSocial(razonSocial.trim()).rut(RutUtils.normalizar(rut)).nombreContacto(nombreContacto)
                .emailContacto(emailContacto).telefono(telefono).rubroId(rubroId).ciudadId(ciudadId)
                .tamanoEmpresaId(pyme ? tamanoEmpresaId : null).descripcionEmpresa(descripcion).sitioWeb(sitioWeb).build();
    }

    static boolean esAdmin(UsuarioActual u) {
        return Roles.ADMINISTRADOR.equals(u.rol());
    }
}
