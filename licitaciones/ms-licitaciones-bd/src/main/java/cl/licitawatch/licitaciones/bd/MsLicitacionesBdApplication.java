package cl.licitawatch.licitaciones.bd;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.licitaciones.bd", version = "2.0", description = "Acceso a datos de licitaciones-bd"))
@SpringBootApplication
public class MsLicitacionesBdApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsLicitacionesBdApplication.class, args);
    }
}
