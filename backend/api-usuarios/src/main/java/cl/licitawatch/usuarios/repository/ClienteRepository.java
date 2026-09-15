package cl.licitawatch.usuarios.repository;

import cl.licitawatch.usuarios.entity.Cliente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUsuario_Id(Long usuarioId);
    boolean existsByRut(String rut);
}
