package cl.licitawatch.usuariosms.repository;

import cl.licitawatch.usuariosms.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
