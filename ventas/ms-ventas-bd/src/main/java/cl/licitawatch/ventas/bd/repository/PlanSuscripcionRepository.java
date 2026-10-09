package cl.licitawatch.ventas.bd.repository;

import cl.licitawatch.ventas.bd.entity.PlanSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanSuscripcionRepository extends JpaRepository<PlanSuscripcion, Integer> {
    Optional<PlanSuscripcion> findByNombreIgnoreCase(String nombre);
}
