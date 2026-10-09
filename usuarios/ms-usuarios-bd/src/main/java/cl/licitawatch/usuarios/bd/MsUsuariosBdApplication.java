package cl.licitawatch.usuarios.bd;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.usuarios.bd", version = "2.0", description = "Acceso a datos de usuarios-bd"))
@SpringBootApplication
public class MsUsuariosBdApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsUsuariosBdApplication.class, args);
    }
}
