package cl.licitawatch.ventas.controller;

import cl.licitawatch.ventas.dto.PlanResponse;
import cl.licitawatch.ventas.repository.PlanSuscripcionRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planes")
public class PlanController {

    private final PlanSuscripcionRepository planRepository;

    public PlanController(PlanSuscripcionRepository planRepository) {
        this.planRepository = planRepository;
    }

    @GetMapping
    public List<PlanResponse> listar() {
        return planRepository.findAll().stream()
                .map(p -> new PlanResponse(p.getId(), p.getNombre().name(), p.getDescripcion(), p.getPrecio(),
                        p.getLimitePublicacionesMes(), p.getLimitePostulacionesMes(),
                        p.isSoportePrioritario(), p.isNotificacionesAutomaticas()))
                .toList();
    }
}
