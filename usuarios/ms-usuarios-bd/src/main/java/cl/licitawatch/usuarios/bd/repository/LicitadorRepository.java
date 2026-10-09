package cl.licitawatch.usuarios.bd.repository;

import cl.licitawatch.usuarios.bd.entity.Licitador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LicitadorRepository extends JpaRepository<Licitador, Integer> {
    Optional<Licitador> findByUsuarioId(Integer usuarioId);

    boolean existsByRut(String rut);
}
