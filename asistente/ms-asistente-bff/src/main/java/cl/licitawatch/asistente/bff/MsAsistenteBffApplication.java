package cl.licitawatch.asistente.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.bff.asistente", version = "2.0", description = "LicitAsist para el frontend"))
@SpringBootApplication
public class MsAsistenteBffApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsAsistenteBffApplication.class, args);
    }
}
