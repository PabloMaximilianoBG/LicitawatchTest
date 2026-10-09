package cl.licitawatch.ventas.ambassador;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(info = @Info(title = "MS.ventas.ambassador", version = "2.0",
        description = "Puente hacia la pasarela de pago (Transbank Webpay Plus, sandbox). El resto del sistema no conoce a Transbank."))
@SpringBootApplication
public class MsVentasAmbassadorApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsVentasAmbassadorApplication.class, args);
    }
}
