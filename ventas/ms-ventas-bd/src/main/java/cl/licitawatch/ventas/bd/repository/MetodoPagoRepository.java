package cl.licitawatch.ventas.bd.repository;

import cl.licitawatch.ventas.bd.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Integer> {
    Optional<MetodoPago> findByNombreIgnoreCase(String nombre);
}
