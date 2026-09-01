package cl.licitawatch.ingestams.repository;

import cl.licitawatch.ingestams.entity.Licitacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LicitacionRepository extends JpaRepository<Licitacion, Long> {
}
