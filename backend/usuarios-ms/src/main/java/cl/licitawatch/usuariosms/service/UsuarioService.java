package cl.licitawatch.usuariosms.service;

import cl.licitawatch.usuariosms.entity.Usuario;
import cl.licitawatch.usuariosms.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario buscar(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario registrar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
