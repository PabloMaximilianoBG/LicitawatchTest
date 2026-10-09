package cl.licitawatch.ventas.bs.service;

import cl.licitawatch.common.dto.PaginaResponse;
import cl.licitawatch.common.seguridad.UsuarioActual;
import cl.licitawatch.ventas.bs.dto.response.MiSuscripcionResponse;
import cl.licitawatch.ventas.bs.dto.response.PlanVigenteResponse;
import cl.licitawatch.ventas.bs.dto.response.SuscripcionResponse;

import java.util.List;

public interface SuscripcionService {
    MiSuscripcionResponse miSuscripcion(UsuarioActual pyme);

    void asignarEstandar(Integer usuarioId);

    PlanVigenteResponse planVigente(Integer usuarioId);

    List<Integer> usuariosPremium(List<Integer> usuarioIds);

    int vencerPremiumExpiradas();

    PaginaResponse<SuscripcionResponse> adminListar(UsuarioActual admin, String estado, String plan, int page, int size);

    SuscripcionResponse adminCancelar(UsuarioActual admin, Integer suscripcionId);
}
