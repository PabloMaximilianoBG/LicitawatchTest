package cl.licitawatch.licitaciones.repository;

import cl.licitawatch.licitaciones.entity.EstadoLicitacion;
import cl.licitawatch.licitaciones.entity.Licitacion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LicitacionRepository extends JpaRepository<Licitacion, Long>, JpaSpecificationExecutor<Licitacion> {
    List<Licitacion> findByEmpresaIdOrderByCreadoEnDesc(Long empresaId);
    List<Licitacion> findByEstadoAndFechaCierreBefore(EstadoLicitacion estado, java.time.LocalDate fecha);
}
