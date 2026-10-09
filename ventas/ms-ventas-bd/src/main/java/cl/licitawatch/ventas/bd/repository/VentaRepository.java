package cl.licitawatch.ventas.bd.repository;

import cl.licitawatch.ventas.bd.entity.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Integer> {
    Page<Venta> findBySuscripcionUsuarioId(Integer usuarioId, Pageable pageable);

    List<Venta> findBySuscripcionIdOrderByIdDesc(Integer suscripcionId);

    @Query("select v from Venta v where v.suscripcion.usuarioId = :usuarioId and v.suscripcion.plan.nombre = 'Premium' "
            + "and not exists (select p from Pago p where p.venta = v) order by v.id desc")
    List<Venta> pendientesPremium(@Param("usuarioId") Integer usuarioId);

    @Query("select coalesce(sum(p.venta.monto), 0) from Pago p where p.estadoPago.nombre = 'Aprobado'")
    BigDecimal totalAprobado();

    @Query("select count(v) from Venta v where not exists (select p from Pago p where p.venta = v)")
    long contarSinPago();
}
