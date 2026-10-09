package cl.licitawatch.usuarios.bs.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.usuarios.bs.dto.request.AdminCrearUsuarioRequest;
import cl.licitawatch.usuarios.bs.dto.request.CambiarEstadoRequest;
import cl.licitawatch.usuarios.bs.dto.request.CambiarRolRequest;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;

/** PPT diap. 6: el Administrador gestiona usuarios y roles. */
public interface AdminUsuarioService {
    PaginaResponse<PerfilResponse> listar(String rol, Boolean activo, String q, int page, int size);

    PerfilResponse obtener(Integer usuarioId);

    PerfilResponse crear(AdminCrearUsuarioRequest request);

    PerfilResponse cambiarEstado(UsuarioActual admin, Integer usuarioId, CambiarEstadoRequest request);

    PerfilResponse cambiarRol(UsuarioActual admin, Integer usuarioId, CambiarRolRequest request);

    void asegurarAdministradorInicial();
}
