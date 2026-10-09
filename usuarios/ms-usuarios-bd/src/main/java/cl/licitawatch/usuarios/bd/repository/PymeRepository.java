package cl.licitawatch.usuarios.bd.repository;

import cl.licitawatch.usuarios.bd.entity.Pyme;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PymeRepository extends JpaRepository<Pyme, Integer> {
    Optional<Pyme> findByUsuarioId(Integer usuarioId);

    boolean existsByRut(String rut);

    List<Pyme> findByRubroIdAndUsuarioActivoTrue(Integer rubroId);
}
