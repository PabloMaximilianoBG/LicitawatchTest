package cl.licitawatch.usuarios.bff.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.usuarios.bff.dto.request.*;
import cl.licitawatch.usuarios.bff.dto.response.*;

import java.util.List;

/** Adaptación de la API de usuarios para el frontend (sin reglas de negocio: esas viven en MS.usuarios.bs). */
public interface UsuarioBffService {
    RegistroResponse registrarLicitador(RegistroLicitadorRequest request);

    RegistroResponse registrarPyme(RegistroPymeRequest request);

    LoginResponse login(LoginRequest request);

    MensajeResponse confirmarCuenta(TokenRequest request);

    MensajeResponse reenviarConfirmacion(EmailRequest request);

    MensajeResponse recuperarPassword(EmailRequest request);

    MensajeResponse restablecerPassword(RestablecerPasswordRequest request);

    PerfilResponse me();

    PerfilResponse actualizarMe(ActualizarPerfilRequest request);

    PerfilPublicoResponse licitador(Integer id);

    PerfilPublicoResponse pyme(Integer id);

    List<CatalogoResponse> rubros();

    List<CatalogoResponse> regiones();

    List<CiudadResponse> ciudades(Integer regionId);

    List<CatalogoResponse> tamanosEmpresa();

    PaginaResponse<PerfilResponse> adminListar(String rol, Boolean activo, String q, int page, int size);

    PerfilResponse adminObtener(Integer id);

    PerfilResponse adminCrear(AdminCrearUsuarioRequest request);

    PerfilResponse adminActualizarPerfil(Integer id, ActualizarPerfilRequest request);

    PerfilResponse adminCambiarEstado(Integer id, CambiarEstadoRequest request);

    PerfilResponse adminCambiarRol(Integer id, CambiarRolRequest request);

    CatalogoResponse adminCrearRubro(RubroRequest request);
}
