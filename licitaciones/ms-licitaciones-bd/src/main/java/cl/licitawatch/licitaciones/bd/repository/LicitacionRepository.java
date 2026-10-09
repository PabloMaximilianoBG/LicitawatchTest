package cl.licitawatch.licitaciones.bd.repository;

import cl.licitawatch.licitaciones.bd.entity.Licitacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;

public interface LicitacionRepository extends JpaRepository<Licitacion, Integer>, JpaSpecificationExecutor<Licitacion> {
    List<Licitacion> findByEstadoLicitacionNombreAndFechaCierreBefore(String estado, LocalDate fecha);
}
