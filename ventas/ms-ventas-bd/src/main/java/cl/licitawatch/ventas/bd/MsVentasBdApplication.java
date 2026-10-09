package cl.licitawatch.ventas.bd;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.ventas.bd", version = "2.0", description = "Acceso a datos de ventas-bd"))
@SpringBootApplication
public class MsVentasBdApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsVentasBdApplication.class, args);
    }
}
