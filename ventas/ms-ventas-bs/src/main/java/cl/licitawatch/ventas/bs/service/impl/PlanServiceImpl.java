package cl.licitawatch.ventas.bs.service.impl;

import cl.licitawatch.ventas.bs.client.VentasBdClient;
import cl.licitawatch.ventas.bs.client.dto.CatalogoDto;
import cl.licitawatch.ventas.bs.config.PlanesProperties;
import cl.licitawatch.ventas.bs.dto.response.PlanResponse;
import cl.licitawatch.ventas.bs.service.PlanService;
import cl.licitawatch.ventas.bs.util.Catalogos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/** Planes de la Pyme (PPT diap. 7). El Licitador no paga: publica sin costo. */
@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {
    private final VentasBdClient bd;
    private final PlanesProperties planes;

    @Override
    public List<PlanResponse> listar() {
        return bd.planes().stream().map(this::plan).toList();
    }

    private PlanResponse plan(CatalogoDto p) {
        if (Catalogos.PREMIUM.equals(p.nombre())) {
            return new PlanResponse(p.id(), p.nombre(), planes.premium().precio(), planes.premium().vigenciaDias(),
                    planes.premium().postulacionesMes(), true, true, "Prioritario", planes.premium().beneficios());
        }
        return new PlanResponse(p.id(), p.nombre(), BigDecimal.ZERO, null, planes.estandar().postulacionesMes(), false, false,
                "Estándar", planes.estandar().beneficios());
    }
}
