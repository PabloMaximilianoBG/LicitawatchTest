package cl.licitawatch.coincidenciasms.config;

import cl.licitawatch.coincidenciasms.entity.Regla;
import cl.licitawatch.coincidenciasms.repository.ReglaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Siembra las reglas de ponderación por defecto si la tabla está vacía.
 * Estos pesos son los que usa MatchingService para calcular el score (0-100).
 */
@Component
@RequiredArgsConstructor
public class ReglaSeeder implements CommandLineRunner {

    private final ReglaRepository reglaRepository;

    @Override
    public void run(String... args) {
        if (reglaRepository.count() == 0) {
            reglaRepository.save(new Regla(null, "rubro", 50));
            reglaRepository.save(new Regla(null, "region", 25));
            reglaRepository.save(new Regla(null, "monto", 25));
        }
    }
}
