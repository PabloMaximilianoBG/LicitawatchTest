package cl.licitawatch.asistente.bs.service.impl;

import cl.licitawatch.asistente.bs.client.VentasClient;
import cl.licitawatch.asistente.bs.service.AccesoService;
import cl.licitawatch.common.exception.ForbiddenException;
import cl.licitawatch.common.exception.ServiceUnavailableException;
import cl.licitawatch.common.seguridad.UsuarioActual;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccesoServiceImpl implements AccesoService {
    private final VentasClient ventas;

    @Override
    public Acceso evaluar(UsuarioActual u) {
        if (u.esAdministrador()) {
            return new Acceso(true, null, "Acceso administrativo");
        }
        if (u.esLicitador()) {
            return new Acceso(true, null, "Acceso libre para Licitadores");
        }
        JsonNode plan;
        try {
            plan = ventas.planVigente(u.usuarioId());
        } catch (Exception e) {
            log.error("No se pudo verificar el plan del usuario {}: {}", u.usuarioId(), e.getMessage());
            // Nunca se concede acceso por defecto si no se puede verificar el plan
            throw new ServiceUnavailableException("No se pudo verificar tu plan. Intenta nuevamente en unos minutos.");
        }
        boolean premium = plan.path("premium").asBoolean(false);
        return new Acceso(premium, plan.path("plan").asText(), premium ? "Incluido en tu plan Premium"
                : "LicitAsist está incluido en el plan Premium. Tu plan actual es " + plan.path("plan").asText() + ".");
    }

    @Override
    public void exigir(UsuarioActual u) {
        Acceso a = evaluar(u);
        if (!a.permitido()) {
            throw new ForbiddenException("PREMIUM_REQUERIDO", a.motivo());
        }
    }
}
