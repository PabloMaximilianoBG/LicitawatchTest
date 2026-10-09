package cl.licitawatch.usuarios.bff.service.impl;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.usuarios.bff.client.UsuarioBsClient;
import cl.licitawatch.usuarios.bff.dto.request.*;
import cl.licitawatch.usuarios.bff.dto.response.*;
import cl.licitawatch.usuarios.bff.service.UsuarioBffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioBffServiceImpl implements UsuarioBffService {
    private final UsuarioBsClient bs;

    @Override
    public RegistroResponse registrarLicitador(RegistroLicitadorRequest request) {
        return bs.registrarLicitador(request);
    }

    @Override
    public RegistroResponse registrarPyme(RegistroPymeRequest request) {
        return bs.registrarPyme(request);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        return bs.login(request);
    }

    @Override
    public MensajeResponse confirmarCuenta(TokenRequest request) {
        return bs.confirmarCuenta(request);
    }

    @Override
    public MensajeResponse reenviarConfirmacion(EmailRequest request) {
        return bs.reenviarConfirmacion(request);
    }

    @Override
    public MensajeResponse recuperarPassword(EmailRequest request) {
        return bs.recuperarPassword(request);
    }

    @Override
    public MensajeResponse restablecerPassword(RestablecerPasswordRequest request) {
        return bs.restablecerPassword(request);
    }

    @Override
    public PerfilResponse me() {
        return bs.me();
    }

    @Override
    public PerfilResponse actualizarMe(ActualizarPerfilRequest request) {
        return bs.actualizarMe(request);
    }

    @Override
    public PerfilPublicoResponse licitador(Integer id) {
        return PerfilPublicoResponse.de(bs.licitador(id));
    }

    @Override
    public PerfilPublicoResponse pyme(Integer id) {
        return PerfilPublicoResponse.de(bs.pyme(id));
    }

    @Override
    public List<CatalogoResponse> rubros() {
        return bs.rubros();
    }

    @Override
    public List<CatalogoResponse> regiones() {
        return bs.regiones();
    }

    @Override
    public List<CiudadResponse> ciudades(Integer regionId) {
        return bs.ciudades(regionId);
    }

    @Override
    public List<CatalogoResponse> tamanosEmpresa() {
        return bs.tamanosEmpresa();
    }

    @Override
    public PaginaResponse<PerfilResponse> adminListar(String rol, Boolean activo, String q, int page, int size) {
        return bs.adminListar(rol, activo, q, page, size);
    }

    @Override
    public PerfilResponse adminObtener(Integer id) {
        return bs.adminObtener(id);
    }

    @Override
    public PerfilResponse adminCrear(AdminCrearUsuarioRequest request) {
        return bs.adminCrear(request);
    }

    @Override
    public PerfilResponse adminActualizarPerfil(Integer id, ActualizarPerfilRequest request) {
        return bs.adminActualizarPerfil(id, request);
    }

    @Override
    public PerfilResponse adminCambiarEstado(Integer id, CambiarEstadoRequest request) {
        return bs.adminCambiarEstado(id, request);
    }

    @Override
    public PerfilResponse adminCambiarRol(Integer id, CambiarRolRequest request) {
        return bs.adminCambiarRol(id, request);
    }

    @Override
    public CatalogoResponse adminCrearRubro(RubroRequest request) {
        return bs.adminCrearRubro(request);
    }
}
