package cl.licitawatch.licitaciones.bd.repository;

import cl.licitawatch.licitaciones.bd.entity.Postulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PostulacionRepository extends JpaRepository<Postulacion, Integer>, JpaSpecificationExecutor<Postulacion> {
    List<Postulacion> findByLicitacionIdOrderByFechaPostulacionAscIdAsc(Integer licitacionId);

    long countByLicitacionId(Integer licitacionId);

    long countByPymeIdAndFechaPostulacionGreaterThanEqual(Integer pymeId, LocalDate desde);

    boolean existsByLicitacionIdAndPymeId(Integer licitacionId, Integer pymeId);

    List<Postulacion> findByLicitacionIdAndPymeId(Integer licitacionId, Integer pymeId);

    @Query("select p.licitacion.id, count(p) from Postulacion p where p.licitacion.id in :ids group by p.licitacion.id")
    List<Object[]> contarPorLicitacion(@Param("ids") Collection<Integer> ids);
}
