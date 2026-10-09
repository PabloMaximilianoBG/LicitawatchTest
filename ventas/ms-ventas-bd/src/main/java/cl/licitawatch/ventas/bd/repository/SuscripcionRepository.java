package cl.licitawatch.ventas.bd.repository;

import cl.licitawatch.ventas.bd.entity.Suscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SuscripcionRepository extends JpaRepository<Suscripcion, Integer>, JpaSpecificationExecutor<Suscripcion> {
    List<Suscripcion> findByUsuarioIdOrderByIdDesc(Integer usuarioId);

    List<Suscripcion> findByUsuarioIdAndEstadoSuscripcionNombre(Integer usuarioId, String estado);

    List<Suscripcion> findByPlanNombreAndEstadoSuscripcionNombre(String plan, String estado);

    @Query("select distinct s.usuarioId from Suscripcion s where s.usuarioId in :ids and s.plan.nombre = 'Premium' "
            + "and s.estadoSuscripcion.nombre = 'Activa'")
    List<Integer> usuariosPremiumActivos(@Param("ids") Collection<Integer> ids);

    long countByPlanNombreAndEstadoSuscripcionNombre(String plan, String estado);
}
