package cl.licitawatch.licitaciones.repository;

import cl.licitawatch.licitaciones.entity.Postulacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostulacionRepository extends JpaRepository<Postulacion, Long> {
    List<Postulacion> findByLicitacion_IdOrderByFechaPostulacionDesc(Long licitacionId);
    List<Postulacion> findByClienteIdOrderByFechaPostulacionDesc(Long clienteId);
    boolean existsByLicitacion_IdAndClienteId(Long licitacionId, Long clienteId);
    Optional<Postulacion> findByIdAndLicitacion_Id(Long id, Long licitacionId);
    long countByLicitacion_Id(Long licitacionId);
}
