package cl.licitawatch.usuarios.repository;

import cl.licitawatch.usuarios.entity.Rol;
import cl.licitawatch.usuarios.entity.RolNombre;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(RolNombre nombre);
}
