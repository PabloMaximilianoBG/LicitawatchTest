package cl.licitawatch.usuarios.repository;

import cl.licitawatch.usuarios.entity.Empresa;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    Optional<Empresa> findByUsuario_Id(Long usuarioId);
    boolean existsByRut(String rut);
}
