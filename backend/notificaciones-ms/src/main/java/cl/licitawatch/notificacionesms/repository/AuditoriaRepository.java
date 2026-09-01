package cl.licitawatch.notificacionesms.repository;

import cl.licitawatch.notificacionesms.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
}
