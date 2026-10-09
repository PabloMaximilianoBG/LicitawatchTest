package cl.licitawatch.ventas.bd.repository;

import cl.licitawatch.ventas.bd.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    Optional<Pago> findByVentaId(Integer ventaId);

    List<Pago> findByVentaIdIn(Collection<Integer> ventaIds);

    long countByEstadoPagoNombre(String estado);

    @Query("select p from Pago p where p.venta.suscripcion.id = :suscripcionId and p.estadoPago.nombre = 'Aprobado' order by p.venta.fecha desc")
    List<Pago> aprobadosDeSuscripcion(@Param("suscripcionId") Integer suscripcionId);
}
