package cl.licitawatch.usuarios.bd.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.exception.BadRequestException;
import cl.licitawatch.common.exception.ConflictException;
import cl.licitawatch.common.exception.ResourceNotFoundException;
import cl.licitawatch.usuarios.bd.dto.request.*;
import cl.licitawatch.usuarios.bd.dto.response.*;
import cl.licitawatch.usuarios.bd.entity.*;
import cl.licitawatch.usuarios.bd.mapper.UsuarioBdMapper;
import cl.licitawatch.usuarios.bd.repository.*;
import cl.licitawatch.usuarios.bd.service.UsuarioDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Acceso a datos de usuario y perfiles. Las reglas de negocio están en MS.usuarios.bs. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioDataServiceImpl implements UsuarioDataService {
    static final String LICITADOR = "Licitador";
    static final String PYME = "Pyme";
    static final String ADMINISTRADOR = "Administrador";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final LicitadorRepository licitadorRepository;
    private final PymeRepository pymeRepository;
    private final AdministradorRepository administradorRepository;
    private final RubroRepository rubroRepository;
    private final CiudadRepository ciudadRepository;
    private final TamanoEmpresaRepository tamanoRepository;
    private final UsuarioBdMapper mapper;

    @Override
    public UsuarioBdResponse obtener(Integer id) {
        return mapper.usuario(usuario(id));
    }

    @Override
    public UsuarioBdResponse buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email.trim()).map(mapper::usuario)
                .orElseThrow(() -> new ResourceNotFoundException("USUARIO_NO_ENCONTRADO", "El usuario no existe"));
    }

    @Override
    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmailIgnoreCase(email.trim());
    }

    @Override
    public boolean existeRut(String rut, String rolNombre) {
        return PYME.equalsIgnoreCase(rolNombre) ? pymeRepository.existsByRut(rut) : licitadorRepository.existsByRut(rut);
    }

    @Override
    public PaginaResponse<PerfilBdResponse> listar(String rol, Boolean activo, String q, int page, int size) {
        Specification<Usuario> spec = Specification.where(UsuarioSpecs.conRol(rol))
                .and(UsuarioSpecs.activo(activo)).and(UsuarioSpecs.texto(q));
        var pagina = usuarioRepository.findAll(spec, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by(Sort.Direction.DESC, "createdAt")));
        return PaginaResponse.de(pagina, this::perfilDe);
    }

    @Override
    @Transactional
    public PerfilBdResponse crear(CrearUsuarioBdRequest r) {
        if (usuarioRepository.existsByEmailIgnoreCase(r.email())) {
            throw new ConflictException("EMAIL_DUPLICADO", "Ya existe una cuenta con ese correo");
        }
        Rol rol = rol(r.rolNombre());
        Usuario u = usuarioRepository.save(Usuario.builder().email(r.email().trim().toLowerCase()).password(r.password())
                .rol(rol).activo(r.activo()).createdAt(LocalDateTime.now()).build());
        return crearPerfilPara(u, rol.getNombre(), r.empresa(), r.administrador());
    }

    @Override
    @Transactional
    public UsuarioBdResponse actualizar(Integer id, ActualizarUsuarioBdRequest r) {
        Usuario u = usuario(id);
        if (r.activo() != null) {
            u.setActivo(r.activo());
        }
        if (r.password() != null) {
            u.setPassword(r.password());
        }
        if (r.rolNombre() != null) {
            u.setRol(rol(r.rolNombre()));
        }
        return mapper.usuario(u);
    }

    @Override
    public PerfilBdResponse perfil(Integer usuarioId) {
        return perfilDe(usuario(usuarioId));
    }

    @Override
    public PerfilesUsuarioResponse perfiles(Integer usuarioId) {
        usuario(usuarioId);
        return new PerfilesUsuarioResponse(
                licitadorRepository.findByUsuarioId(usuarioId).map(Licitador::getId).orElse(null),
                pymeRepository.findByUsuarioId(usuarioId).map(Pyme::getId).orElse(null),
                administradorRepository.findByUsuarioId(usuarioId).map(Administrador::getId).orElse(null));
    }

    @Override
    @Transactional
    public PerfilBdResponse crearPerfil(Integer usuarioId, CrearPerfilBdRequest r) {
        Usuario u = usuario(usuarioId);
        return crearPerfilPara(u, rol(r.rolNombre()).getNombre(), r.empresa(), r.administrador());
    }

    @Override
    @Transactional
    public PerfilBdResponse actualizarPerfil(Integer usuarioId, ActualizarPerfilBdRequest r) {
        Usuario u = usuario(usuarioId);
        String rol = u.getRol().getNombre();
        if (ADMINISTRADOR.equalsIgnoreCase(rol)) {
            Administrador a = administradorRepository.findByUsuarioId(usuarioId).orElseThrow(this::perfilNoEncontrado);
            if (r.administrador() == null) {
                throw new BadRequestException("Faltan los datos del administrador");
            }
            a.setNombre(r.administrador().nombre().trim());
            a.setArea(r.administrador().area().trim());
            return mapper.administrador(a);
        }
        DatosEmpresaBdRequest e = r.empresa();
        if (e == null) {
            throw new BadRequestException("Faltan los datos de la empresa");
        }
        if (LICITADOR.equalsIgnoreCase(rol)) {
            Licitador l = licitadorRepository.findByUsuarioId(usuarioId).orElseThrow(this::perfilNoEncontrado);
            l.setRazonSocial(e.razonSocial() != null ? e.razonSocial().trim() : l.getRazonSocial());
            l.setNombreContacto(e.nombreContacto().trim());
            l.setEmailContacto(e.emailContacto().trim());
            l.setTelefono(e.telefono().trim());
            l.setRubro(rubro(e.rubroId()));
            l.setCiudad(ciudad(e.ciudadId()));
            l.setDescripcionEmpresa(e.descripcionEmpresa().trim());
            l.setSitioWeb(vacioANull(e.sitioWeb()));
            l.setUpdatedAt(LocalDateTime.now());
            return mapper.licitador(l);
        }
        Pyme p = pymeRepository.findByUsuarioId(usuarioId).orElseThrow(this::perfilNoEncontrado);
        p.setRazonSocial(e.razonSocial() != null ? e.razonSocial().trim() : p.getRazonSocial());
        p.setNombreContacto(e.nombreContacto().trim());
        p.setEmailContacto(e.emailContacto().trim());
        p.setTelefono(e.telefono().trim());
        p.setRubro(rubro(e.rubroId()));
        p.setCiudad(ciudad(e.ciudadId()));
        if (e.tamanoEmpresaId() != null) {
            p.setTamanoEmpresa(tamano(e.tamanoEmpresaId()));
        }
        p.setDescripcionEmpresa(e.descripcionEmpresa().trim());
        p.setSitioWeb(vacioANull(e.sitioWeb()));
        p.setUpdatedAt(LocalDateTime.now());
        return mapper.pyme(p);
    }

    @Override
    public PerfilBdResponse licitador(Integer id) {
        return licitadorRepository.findById(id).map(mapper::licitador)
                .orElseThrow(() -> new ResourceNotFoundException("LICITADOR_NO_ENCONTRADO", "El licitador no existe"));
    }

    @Override
    public List<PerfilBdResponse> licitadores(List<Integer> ids) {
        return licitadorRepository.findAllById(ids).stream().map(mapper::licitador).toList();
    }

    @Override
    public PerfilBdResponse pyme(Integer id) {
        return pymeRepository.findById(id).map(mapper::pyme)
                .orElseThrow(() -> new ResourceNotFoundException("PYME_NO_ENCONTRADA", "La pyme no existe"));
    }

    @Override
    public List<PerfilBdResponse> pymes(List<Integer> ids) {
        return pymeRepository.findAllById(ids).stream().map(mapper::pyme).toList();
    }

    @Override
    public List<PerfilBdResponse> pymesPorRubro(Integer rubroId) {
        return pymeRepository.findByRubroIdAndUsuarioActivoTrue(rubroId).stream().map(mapper::pyme).toList();
    }

    // ------------------------------------------------------------------ apoyo

    private PerfilBdResponse crearPerfilPara(Usuario u, String rol, DatosEmpresaBdRequest e, DatosAdministradorBdRequest a) {
        LocalDateTime ahora = LocalDateTime.now();
        if (ADMINISTRADOR.equalsIgnoreCase(rol)) {
            if (a == null) {
                throw new BadRequestException("Faltan los datos del administrador (nombre, área)");
            }
            if (administradorRepository.findByUsuarioId(u.getId()).isPresent()) {
                return mapper.administrador(administradorRepository.findByUsuarioId(u.getId()).get());
            }
            return mapper.administrador(administradorRepository.save(Administrador.builder().usuario(u)
                    .nombre(a.nombre().trim()).area(a.area().trim()).build()));
        }
        if (e == null || e.rut() == null || e.razonSocial() == null) {
            throw new BadRequestException("Faltan los datos de la empresa");
        }
        if (LICITADOR.equalsIgnoreCase(rol)) {
            if (licitadorRepository.existsByRut(e.rut())) {
                throw new ConflictException("RUT_DUPLICADO", "Ya existe un licitador con ese RUT");
            }
            return mapper.licitador(licitadorRepository.save(Licitador.builder().usuario(u).razonSocial(e.razonSocial().trim())
                    .rut(e.rut()).nombreContacto(e.nombreContacto().trim()).emailContacto(e.emailContacto().trim())
                    .telefono(e.telefono().trim()).rubro(rubro(e.rubroId())).ciudad(ciudad(e.ciudadId()))
                    .descripcionEmpresa(e.descripcionEmpresa().trim()).sitioWeb(vacioANull(e.sitioWeb())).updatedAt(ahora).build()));
        }
        if (pymeRepository.existsByRut(e.rut())) {
            throw new ConflictException("RUT_DUPLICADO", "Ya existe una pyme con ese RUT");
        }
        if (e.tamanoEmpresaId() == null) {
            throw new BadRequestException("Falta el tamaño de la empresa");
        }
        return mapper.pyme(pymeRepository.save(Pyme.builder().usuario(u).razonSocial(e.razonSocial().trim())
                .rut(e.rut()).nombreContacto(e.nombreContacto().trim()).emailContacto(e.emailContacto().trim())
                .telefono(e.telefono().trim()).rubro(rubro(e.rubroId())).ciudad(ciudad(e.ciudadId()))
                .tamanoEmpresa(tamano(e.tamanoEmpresaId())).descripcionEmpresa(e.descripcionEmpresa().trim())
                .sitioWeb(vacioANull(e.sitioWeb())).updatedAt(ahora).build()));
    }

    private PerfilBdResponse perfilDe(Usuario u) {
        String rol = u.getRol().getNombre();
        if (LICITADOR.equalsIgnoreCase(rol)) {
            return licitadorRepository.findByUsuarioId(u.getId()).map(mapper::licitador).orElseGet(() -> mapper.soloUsuario(u));
        }
        if (PYME.equalsIgnoreCase(rol)) {
            return pymeRepository.findByUsuarioId(u.getId()).map(mapper::pyme).orElseGet(() -> mapper.soloUsuario(u));
        }
        return administradorRepository.findByUsuarioId(u.getId()).map(mapper::administrador).orElseGet(() -> mapper.soloUsuario(u));
    }

    private Usuario usuario(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USUARIO_NO_ENCONTRADO", "El usuario no existe"));
    }

    private Rol rol(String nombre) {
        return rolRepository.findByNombreIgnoreCase(nombre)
                .orElseThrow(() -> new BadRequestException("ROL_INVALIDO", "El rol no existe"));
    }

    private Rubro rubro(Integer id) {
        return rubroRepository.findById(id).orElseThrow(() -> new BadRequestException("RUBRO_INVALIDO", "El rubro no existe"));
    }

    private Ciudad ciudad(Integer id) {
        return ciudadRepository.findById(id).orElseThrow(() -> new BadRequestException("CIUDAD_INVALIDA", "La ciudad no existe"));
    }

    private TamanoEmpresa tamano(Integer id) {
        return tamanoRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("TAMANO_INVALIDO", "El tamaño de empresa no existe"));
    }

    private ResourceNotFoundException perfilNoEncontrado() {
        return new ResourceNotFoundException("PERFIL_NO_ENCONTRADO", "El usuario no tiene perfil para su rol");
    }

    private static String vacioANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
