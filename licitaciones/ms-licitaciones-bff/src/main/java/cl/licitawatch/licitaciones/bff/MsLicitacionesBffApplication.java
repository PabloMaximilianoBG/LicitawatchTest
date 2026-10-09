package cl.licitawatch.licitaciones.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.bff.licitaciones", version = "2.0", description = "API de licitaciones y postulaciones para el frontend"))
@SpringBootApplication
public class MsLicitacionesBffApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsLicitacionesBffApplication.class, args);
    }
}
