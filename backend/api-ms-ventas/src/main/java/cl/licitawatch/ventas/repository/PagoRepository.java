package cl.licitawatch.ventas.repository;

import cl.licitawatch.ventas.entity.Pago;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByToken(String token);
    Optional<Pago> findByVenta_Id(Long ventaId);

    /**
     * Bloqueo pesimista sobre la fila del pago mientras dura la transaccion
     * de confirmar(). Sin esto, dos llamadas casi simultaneas a
     * POST /api/ventas/confirmar con el mismo token (por ejemplo, React
     * StrictMode invocando un efecto dos veces en desarrollo, o el usuario
     * recargando /pago/resultado justo a tiempo) pueden leer ambas el mismo
     * estado PENDIENTE antes de que ninguna lo actualice, y las dos terminan
     * llamando a confirmar() en Transbank al mismo tiempo - la pasarela
     * rechaza la segunda con "Transaction already locked by another
     * process". Con el lock, la segunda transaccion espera a que la primera
     * termine y confirme, y al releer ve que el pago ya no esta PENDIENTE.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pago p where p.token = :token")
    Optional<Pago> findByTokenParaActualizar(String token);
}
