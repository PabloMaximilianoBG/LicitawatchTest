package cl.licitawatch.ventas.bs;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@OpenAPIDefinition(info = @Info(title = "MS.ventas.bs", version = "2.0", description = "Planes, suscripciones, ventas y pagos de las Pymes"))
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
@SpringBootApplication
public class MsVentasBsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MsVentasBsApplication.class, args);
    }
}
