package cl.licitawatch.licitaciones.bd.repository;

import cl.licitawatch.licitaciones.bd.entity.EstadoLicitacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoLicitacionRepository extends JpaRepository<EstadoLicitacion, Integer> {
    Optional<EstadoLicitacion> findByNombreIgnoreCase(String nombre);
}
