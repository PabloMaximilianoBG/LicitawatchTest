package cl.licitawatch.licitaciones.bd.repository;

import cl.licitawatch.licitaciones.bd.entity.EstadoPostulacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoPostulacionRepository extends JpaRepository<EstadoPostulacion, Integer> {
    Optional<EstadoPostulacion> findByNombreIgnoreCase(String nombre);
}
