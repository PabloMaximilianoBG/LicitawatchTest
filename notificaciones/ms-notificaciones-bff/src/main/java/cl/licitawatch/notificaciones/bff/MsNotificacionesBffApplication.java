package cl.licitawatch.notificaciones.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.bff.notificaciones", version = "2.0", description = "Historial de avisos y soporte para el frontend"))
@SpringBootApplication
public class MsNotificacionesBffApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsNotificacionesBffApplication.class, args);
    }
}
