package cl.licitawatch.ventas.bd.repository;

import cl.licitawatch.ventas.bd.entity.EstadoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoPagoRepository extends JpaRepository<EstadoPago, Integer> {
    Optional<EstadoPago> findByNombreIgnoreCase(String nombre);
}
