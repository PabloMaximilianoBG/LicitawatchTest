package cl.licitawatch.usuarios.config;

import cl.licitawatch.usuarios.entity.Administrador;
import cl.licitawatch.usuarios.entity.RolNombre;
import cl.licitawatch.usuarios.entity.Usuario;
import cl.licitawatch.usuarios.repository.AdministradorRepository;
import cl.licitawatch.usuarios.repository.RolRepository;
import cl.licitawatch.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Alta de Administrador restringida (seccion 3.1 del diseno): solo otro
 * Administrador puede crear uno via API, o se siembra un unico admin inicial
 * aqui al levantar el proyecto por primera vez (si no existe ninguno).
 */
@Component
@Order(2)
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final String seedEmail;
    private final String seedPassword;

    public AdminSeeder(
            UsuarioRepository usuarioRepository,
            AdministradorRepository administradorRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            @Value("${licitawatch.admin.seed.email}") String seedEmail,
            @Value("${licitawatch.admin.seed.password}") String seedPassword) {
        this.usuarioRepository = usuarioRepository;
        this.administradorRepository = administradorRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedEmail = seedEmail;
        this.seedPassword = seedPassword;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.countByRol_Nombre(RolNombre.ADMINISTRADOR) > 0) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setEmail(seedEmail);
        usuario.setContrasenaHash(passwordEncoder.encode(seedPassword));
        usuario.setRol(rolRepository.findByNombre(RolNombre.ADMINISTRADOR).orElseThrow());
        usuario.setActivo(true);
        usuarioRepository.save(usuario);

        Administrador administrador = new Administrador();
        administrador.setUsuario(usuario);
        administrador.setArea("Plataforma");
        administradorRepository.save(administrador);

        log.warn("Administrador inicial creado con email '{}'. Cambia la contrasena por defecto en produccion "
                + "(variables ADMIN_SEED_EMAIL / ADMIN_SEED_PASSWORD).", seedEmail);
    }
}
