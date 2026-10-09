package cl.licitawatch.usuarios.bs.config;

import cl.licitawatch.usuarios.bs.service.AdminUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Crea el administrador inicial (ADMIN_EMAIL / ADMIN_PASSWORD) si aún no existe. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicialRunner implements ApplicationRunner {
    private final AdminUsuarioService adminUsuarioService;

    @Override
    public void run(ApplicationArguments args) {
        for (int intento = 1; intento <= 10; intento++) {
            try {
                adminUsuarioService.asegurarAdministradorInicial();
                return;
            } catch (Exception e) {
                log.warn("No se pudo verificar el administrador inicial (intento {}/10): {}", intento, e.getMessage());
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
