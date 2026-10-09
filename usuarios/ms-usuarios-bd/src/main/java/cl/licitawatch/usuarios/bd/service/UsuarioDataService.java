package cl.licitawatch.usuarios.bd.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.usuarios.bd.dto.request.*;
import cl.licitawatch.usuarios.bd.dto.response.*;

import java.util.List;

public interface UsuarioDataService {
    UsuarioBdResponse obtener(Integer id);

    UsuarioBdResponse buscarPorEmail(String email);

    boolean existeEmail(String email);

    boolean existeRut(String rut, String rolNombre);

    PaginaResponse<PerfilBdResponse> listar(String rol, Boolean activo, String q, int page, int size);

    PerfilBdResponse crear(CrearUsuarioBdRequest request);

    UsuarioBdResponse actualizar(Integer id, ActualizarUsuarioBdRequest request);

    PerfilBdResponse perfil(Integer usuarioId);

    PerfilesUsuarioResponse perfiles(Integer usuarioId);

    PerfilBdResponse crearPerfil(Integer usuarioId, CrearPerfilBdRequest request);

    PerfilBdResponse actualizarPerfil(Integer usuarioId, ActualizarPerfilBdRequest request);

    PerfilBdResponse licitador(Integer id);

    List<PerfilBdResponse> licitadores(List<Integer> ids);

    PerfilBdResponse pyme(Integer id);

    List<PerfilBdResponse> pymes(List<Integer> ids);

    List<PerfilBdResponse> pymesPorRubro(Integer rubroId);
}
