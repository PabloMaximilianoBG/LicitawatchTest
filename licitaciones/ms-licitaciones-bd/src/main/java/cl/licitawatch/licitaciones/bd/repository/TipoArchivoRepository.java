package cl.licitawatch.licitaciones.bd.repository;

import cl.licitawatch.licitaciones.bd.entity.TipoArchivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoArchivoRepository extends JpaRepository<TipoArchivo, Integer> {
    Optional<TipoArchivo> findByNombreIgnoreCase(String nombre);
}
