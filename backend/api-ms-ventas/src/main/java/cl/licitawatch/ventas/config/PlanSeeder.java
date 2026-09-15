package cl.licitawatch.ventas.config;

import cl.licitawatch.ventas.entity.NombrePlan;
import cl.licitawatch.ventas.entity.PlanSuscripcion;
import cl.licitawatch.ventas.repository.PlanSuscripcionRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra los 2 planes fijos (Estandar/Premium) descritos en la seccion 3.3 y
 * en la PPT (slide 6), si no existen. Precios de referencia para el MVP -
 * ajustables despues desde la base de datos, no hay endpoint de "crear plan"
 * porque el catalogo de planes es fijo por diseno.
 */
@Component
public class PlanSeeder implements CommandLineRunner {

    private final PlanSuscripcionRepository planRepository;

    public PlanSeeder(PlanSuscripcionRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(String... args) {
        planRepository.findByNombre(NombrePlan.ESTANDAR).orElseGet(() -> {
            PlanSuscripcion plan = new PlanSuscripcion();
            plan.setNombre(NombrePlan.ESTANDAR);
            plan.setDescripcion("Plan gratuito, incluido por defecto en toda cuenta nueva - acceso funcional con limite mensual.");
            plan.setPrecio(BigDecimal.ZERO);
            plan.setLimitePublicacionesMes(5);
            plan.setLimitePostulacionesMes(10);
            plan.setSoportePrioritario(false);
            plan.setNotificacionesAutomaticas(false);
            return planRepository.save(plan);
        });

        planRepository.findByNombre(NombrePlan.PREMIUM).orElseGet(() -> {
            PlanSuscripcion plan = new PlanSuscripcion();
            plan.setNombre(NombrePlan.PREMIUM);
            plan.setDescripcion("Publicaciones y postulaciones ilimitadas, prioridad de visibilidad y soporte prioritario.");
            plan.setPrecio(new BigDecimal("24990"));
            plan.setLimitePublicacionesMes(null);
            plan.setLimitePostulacionesMes(null);
            plan.setSoportePrioritario(true);
            plan.setNotificacionesAutomaticas(true);
            return planRepository.save(plan);
        });
    }
}
