package cl.licitawatch.usuarios.repository;

import cl.licitawatch.usuarios.entity.Administrador;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministradorRepository extends JpaRepository<Administrador, Long> {
    Optional<Administrador> findByUsuario_Id(Long usuarioId);
}
