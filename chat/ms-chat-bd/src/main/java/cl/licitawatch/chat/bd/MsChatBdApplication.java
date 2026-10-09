package cl.licitawatch.chat.bd;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.chat.bd", version = "2.0", description = "Acceso a datos de chat-bd"))
@SpringBootApplication
public class MsChatBdApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsChatBdApplication.class, args);
    }
}
