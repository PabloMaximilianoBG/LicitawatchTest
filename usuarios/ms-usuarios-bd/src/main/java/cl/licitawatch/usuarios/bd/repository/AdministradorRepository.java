package cl.licitawatch.usuarios.bd.repository;

import cl.licitawatch.usuarios.bd.entity.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdministradorRepository extends JpaRepository<Administrador, Integer> {
    Optional<Administrador> findByUsuarioId(Integer usuarioId);
}
