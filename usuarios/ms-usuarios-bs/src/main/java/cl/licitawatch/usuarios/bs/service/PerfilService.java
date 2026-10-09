package cl.licitawatch.usuarios.bs.service;

import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.usuarios.bs.dto.request.ActualizarPerfilRequest;
import cl.licitawatch.usuarios.bs.dto.response.PerfilResponse;
import cl.licitawatch.usuarios.bs.dto.response.UsuarioBasicoResponse;

import java.util.List;

public interface PerfilService {
    PerfilResponse miPerfil(UsuarioActual usuario);

    PerfilResponse actualizarMiPerfil(UsuarioActual usuario, ActualizarPerfilRequest request);

    PerfilResponse actualizarPerfil(Integer usuarioId, ActualizarPerfilRequest request);

    UsuarioBasicoResponse usuarioBasico(Integer usuarioId);

    PerfilResponse licitador(Integer licitadorId);

    List<PerfilResponse> licitadores(List<Integer> ids);

    PerfilResponse pyme(Integer pymeId);

    List<PerfilResponse> pymes(List<Integer> ids);

    List<PerfilResponse> pymesPorRubro(Integer rubroId);
}
