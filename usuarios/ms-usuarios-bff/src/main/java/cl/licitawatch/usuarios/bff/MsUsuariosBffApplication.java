package cl.licitawatch.usuarios.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.bff.usuarios", version = "2.0", description = "API de usuarios para el frontend (vía api-gateway)"))
@SpringBootApplication
public class MsUsuariosBffApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsUsuariosBffApplication.class, args);
    }
}
