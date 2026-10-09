package cl.licitawatch.chat.bs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@OpenAPIDefinition(info = @Info(title = "MS.chat.bs", version = "2.0", description = "Chat privado Licitador-Pyme"))
@EnableAsync
@SpringBootApplication
public class MsChatBsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsChatBsApplication.class, args);
    }
}
