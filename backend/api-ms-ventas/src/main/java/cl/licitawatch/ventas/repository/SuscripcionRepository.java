package cl.licitawatch.ventas.repository;

import cl.licitawatch.ventas.entity.Suscripcion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {
    List<Suscripcion> findByUsuarioIdOrderByCreadoEnDesc(Long usuarioId);
}
