package cl.licitawatch.chat.bd.repository;

import cl.licitawatch.chat.bd.entity.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {
    List<Mensaje> findByConversacionIdAndIdGreaterThanOrderByIdAsc(Integer conversacionId, Integer despuesDeId);

    Optional<Mensaje> findFirstByConversacionIdOrderByIdDesc(Integer conversacionId);

    long countByConversacionIdAndEmisorIdNotAndLeidoFalse(Integer conversacionId, Integer lectorId);

    @Modifying
    @Query("update Mensaje m set m.leido = true where m.conversacion.id = :conversacionId and m.emisorId <> :lectorId and m.leido = false")
    int marcarLeidos(@Param("conversacionId") Integer conversacionId, @Param("lectorId") Integer lectorId);

    @Modifying
    @Query("delete from Mensaje m where m.conversacion.id in :ids")
    int eliminarDeConversaciones(@Param("ids") List<Integer> conversacionIds);
}
