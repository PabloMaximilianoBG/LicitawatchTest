package cl.licitawatch.usuarios.bs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.usuarios.bs", version = "2.0", description = "Reglas de negocio de usuarios"))
@SpringBootApplication
public class MsUsuariosBsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsUsuariosBsApplication.class, args);
    }
}
