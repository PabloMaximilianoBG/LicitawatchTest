package cl.licitawatch.coincidenciasms.repository;

import cl.licitawatch.coincidenciasms.entity.PreferenciaLocal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreferenciaLocalRepository extends JpaRepository<PreferenciaLocal, Long> {
    List<PreferenciaLocal> findByUsuarioId(Long usuarioId);
}
