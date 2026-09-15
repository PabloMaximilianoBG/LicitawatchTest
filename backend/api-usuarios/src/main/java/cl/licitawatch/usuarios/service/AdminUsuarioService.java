package cl.licitawatch.usuarios.service;

import cl.licitawatch.usuarios.dto.CrearAdministradorRequest;
import cl.licitawatch.usuarios.dto.UsuarioAdminResponse;
import cl.licitawatch.usuarios.dto.UsuarioResumen;
import cl.licitawatch.usuarios.entity.Administrador;
import cl.licitawatch.usuarios.entity.RolNombre;
import cl.licitawatch.usuarios.entity.Usuario;
import cl.licitawatch.usuarios.exception.EmailYaRegistradoException;
import cl.licitawatch.usuarios.exception.RecursoNoEncontradoException;
import cl.licitawatch.usuarios.repository.AdministradorRepository;
import cl.licitawatch.usuarios.repository.RolRepository;
import cl.licitawatch.usuarios.repository.UsuarioRepository;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioService(
            UsuarioRepository usuarioRepository,
            AdministradorRepository administradorRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.administradorRepository = administradorRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioAdminResponse(u.getId(), u.getEmail(), u.getRol().getNombre().name(), u.isActivo(), u.getCreadoEn()))
                .toList();
    }

    @Transactional
    public void actualizarActivo(Long usuarioId, boolean activo) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResumen crearAdministrador(CrearAdministradorRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new EmailYaRegistradoException(req.email());
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(req.email());
        usuario.setContrasenaHash(passwordEncoder.encode(req.contrasena()));
        usuario.setRol(rolRepository.findByNombre(RolNombre.ADMINISTRADOR).orElseThrow());
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        Administrador administrador = new Administrador();
        administrador.setUsuario(usuario);
        administrador.setArea(req.area());
        administradorRepository.save(administrador);

        return new UsuarioResumen(usuario.getId(), usuario.getEmail(), RolNombre.ADMINISTRADOR.name());
    }
}
