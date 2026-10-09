package cl.licitawatch.ventas.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.bff.ventas", version = "2.0", description = "Planes, suscripción y pago Premium para el frontend"))
@SpringBootApplication
public class MsVentasBffApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsVentasBffApplication.class, args);
    }
}
