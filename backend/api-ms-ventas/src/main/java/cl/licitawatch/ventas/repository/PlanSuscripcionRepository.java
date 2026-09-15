package cl.licitawatch.ventas.repository;

import cl.licitawatch.ventas.entity.NombrePlan;
import cl.licitawatch.ventas.entity.PlanSuscripcion;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Long> {
    Optional<PlanSuscripcion> findByNombre(NombrePlan nombre);
}
