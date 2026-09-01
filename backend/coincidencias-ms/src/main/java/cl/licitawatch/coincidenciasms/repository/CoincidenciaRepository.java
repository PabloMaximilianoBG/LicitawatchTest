package cl.licitawatch.coincidenciasms.repository;

import cl.licitawatch.coincidenciasms.entity.Coincidencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoincidenciaRepository extends JpaRepository<Coincidencia, Long> {
    Optional<Coincidencia> findByLicitacionIdAndUsuarioId(Long licitacionId, Long usuarioId);

    List<Coincidencia> findByUsuarioIdOrderByScoreDesc(Long usuarioId);
}
