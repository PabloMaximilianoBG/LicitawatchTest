package cl.licitawatch.ventas.bd.repository;

import cl.licitawatch.ventas.bd.entity.EstadoSuscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoSuscripcionRepository extends JpaRepository<EstadoSuscripcion, Integer> {
    Optional<EstadoSuscripcion> findByNombreIgnoreCase(String nombre);
}
