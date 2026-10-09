package cl.licitawatch.licitaciones.bs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(info = @Info(title = "MS.licitaciones.bs", version = "2.0", description = "Reglas de negocio de licitaciones y postulaciones"))
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class MsLicitacionesBsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsLicitacionesBsApplication.class, args);
    }
}
