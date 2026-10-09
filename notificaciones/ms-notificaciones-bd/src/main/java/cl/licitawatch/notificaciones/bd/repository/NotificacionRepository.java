package cl.licitawatch.notificaciones.bd.repository;

import cl.licitawatch.notificaciones.bd.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificacionRepository extends JpaRepository<Notificacion, Integer>, JpaSpecificationExecutor<Notificacion> {
}
