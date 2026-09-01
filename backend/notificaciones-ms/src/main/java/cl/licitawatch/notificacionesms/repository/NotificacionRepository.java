package cl.licitawatch.notificacionesms.repository;

import cl.licitawatch.notificacionesms.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
}
