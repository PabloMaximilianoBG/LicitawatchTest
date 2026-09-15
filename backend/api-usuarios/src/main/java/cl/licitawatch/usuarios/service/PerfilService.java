package cl.licitawatch.usuarios.service;

import cl.licitawatch.usuarios.dto.ActualizarPerfilClienteRequest;
import cl.licitawatch.usuarios.dto.ActualizarPerfilEmpresaRequest;
import cl.licitawatch.usuarios.dto.PerfilResponse;
import cl.licitawatch.usuarios.dto.UsuarioInternoResponse;
import cl.licitawatch.usuarios.entity.Administrador;
import cl.licitawatch.usuarios.entity.Cliente;
import cl.licitawatch.usuarios.entity.Empresa;
import cl.licitawatch.usuarios.entity.RolNombre;
import cl.licitawatch.usuarios.entity.Usuario;
import cl.licitawatch.usuarios.exception.ApiException;
import cl.licitawatch.usuarios.exception.RecursoNoEncontradoException;
import cl.licitawatch.usuarios.repository.AdministradorRepository;
import cl.licitawatch.usuarios.repository.ClienteRepository;
import cl.licitawatch.usuarios.repository.EmpresaRepository;
import cl.licitawatch.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PerfilService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;

    public PerfilService(
            UsuarioRepository usuarioRepository,
            EmpresaRepository empresaRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
    }

    @Transactional(readOnly = true)
    public PerfilResponse obtenerPerfil(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        return construirPerfil(usuario);
    }

    @Transactional
    public PerfilResponse actualizarPerfilEmpresa(Long usuarioId, ActualizarPerfilEmpresaRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);
        exigirRol(usuario, RolNombre.EMPRESA);

        Empresa empresa = empresaRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
        empresa.setRazonSocial(req.razonSocial());
        empresa.setRubro(req.rubro());
        empresaRepository.save(empresa);

        return construirPerfil(usuario);
    }

    @Transactional
    public PerfilResponse actualizarPerfilCliente(Long usuarioId, ActualizarPerfilClienteRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);
        exigirRol(usuario, RolNombre.CLIENTE);

        Cliente cliente = clienteRepository.findByUsuario_Id(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
        cliente.setNombreContacto(req.nombreContacto());
        clienteRepository.save(cliente);

        return construirPerfil(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioInternoResponse obtenerInfoInterna(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        String nombre = switch (usuario.getRol().getNombre()) {
            case EMPRESA -> empresaRepository.findByUsuario_Id(usuarioId).map(Empresa::getRazonSocial).orElse(null);
            case CLIENTE -> clienteRepository.findByUsuario_Id(usuarioId).map(Cliente::getNombreContacto).orElse(null);
            case ADMINISTRADOR -> "Administrador";
        };
        return new UsuarioInternoResponse(usuario.getId(), usuario.getEmail(),
                usuario.getRol().getNombre().name(), nombre, usuario.isActivo());
    }

    private PerfilResponse construirPerfil(Usuario usuario) {
        RolNombre rol = usuario.getRol().getNombre();
        String razonSocial = null;
        String rubro = null;
        String nombreContacto = null;
        String rut = null;
        String area = null;

        switch (rol) {
            case EMPRESA -> {
                Empresa empresa = empresaRepository.findByUsuario_Id(usuario.getId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Empresa no encontrada"));
                razonSocial = empresa.getRazonSocial();
                rubro = empresa.getRubro();
                rut = empresa.getRut();
            }
            case CLIENTE -> {
                Cliente cliente = clienteRepository.findByUsuario_Id(usuario.getId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
                nombreContacto = cliente.getNombreContacto();
                rut = cliente.getRut();
            }
            case ADMINISTRADOR -> {
                Administrador administrador = administradorRepository.findByUsuario_Id(usuario.getId())
                        .orElseThrow(() -> new RecursoNoEncontradoException("Administrador no encontrado"));
                area = administrador.getArea();
            }
        }

        return new PerfilResponse(usuario.getId(), usuario.getEmail(), rol.name(), usuario.isActivo(),
                razonSocial, rubro, nombreContacto, rut, area);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
    }

    private void exigirRol(Usuario usuario, RolNombre esperado) {
        if (usuario.getRol().getNombre() != esperado) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Esta accion no esta disponible para tu rol");
        }
    }
}
