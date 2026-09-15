package cl.licitawatch.usuarios.config;

import cl.licitawatch.usuarios.entity.Rol;
import cl.licitawatch.usuarios.entity.RolNombre;
import cl.licitawatch.usuarios.repository.RolRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Siembra las 3 filas fijas de la tabla rol si no existen. Corre en dev
 * (Postgres, tabla creada por Flyway) y en tests (H2, tabla creada por
 * Hibernate ddl-auto) con el mismo codigo.
 */
@Component
@Order(1)
public class RolSeeder implements CommandLineRunner {

    private final RolRepository rolRepository;

    public RolSeeder(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    public void run(String... args) {
        for (RolNombre nombre : RolNombre.values()) {
            rolRepository.findByNombre(nombre).orElseGet(() -> rolRepository.save(new Rol(nombre)));
        }
    }
}
