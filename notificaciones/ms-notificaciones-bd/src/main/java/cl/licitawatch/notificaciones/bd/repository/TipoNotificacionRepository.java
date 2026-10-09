package cl.licitawatch.notificaciones.bd.repository;

import cl.licitawatch.notificaciones.bd.entity.TipoNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoNotificacionRepository extends JpaRepository<TipoNotificacion, Integer> {
    Optional<TipoNotificacion> findByNombreIgnoreCase(String nombre);
}
