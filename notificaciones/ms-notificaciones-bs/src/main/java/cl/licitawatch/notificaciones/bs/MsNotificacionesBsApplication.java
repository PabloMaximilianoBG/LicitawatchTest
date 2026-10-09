package cl.licitawatch.notificaciones.bs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.notificaciones.bs", version = "2.0", description = "Alertas y confirmaciones por correo (Gmail SMTP)"))
@SpringBootApplication
public class MsNotificacionesBsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsNotificacionesBsApplication.class, args);
    }
}
