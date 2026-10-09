package cl.licitawatch.chat.bd.repository;

import cl.licitawatch.chat.bd.entity.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {
    Optional<Conversacion> findByPostulacionId(Integer postulacionId);

    boolean existsByPostulacionId(Integer postulacionId);

    List<Conversacion> findByLicitadorIdOrderByCreatedAtDesc(Integer licitadorId);

    List<Conversacion> findByPymeIdOrderByCreatedAtDesc(Integer pymeId);

    List<Conversacion> findByPostulacionIdIn(Collection<Integer> postulacionIds);
}
