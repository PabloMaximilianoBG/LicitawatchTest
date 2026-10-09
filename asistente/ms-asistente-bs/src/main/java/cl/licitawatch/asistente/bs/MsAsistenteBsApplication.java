package cl.licitawatch.asistente.bs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.asistente.bs", version = "2.0",
        description = "LicitAsist: orquesta datos reales de la plataforma y Groq (sin base de datos propia)"))
@SpringBootApplication
public class MsAsistenteBsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsAsistenteBsApplication.class, args);
    }
}
