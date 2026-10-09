package cl.licitawatch.chat.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.bff.chat", version = "2.0", description = "Chat privado Licitador-Pyme para el frontend (polling)"))
@SpringBootApplication
public class MsChatBffApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsChatBffApplication.class, args);
    }
}
