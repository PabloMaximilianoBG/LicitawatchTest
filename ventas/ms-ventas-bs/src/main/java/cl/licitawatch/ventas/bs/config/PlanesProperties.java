package cl.licitawatch.ventas.bs.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.List;

/** Condiciones comerciales de los planes de la PPT (diap. 7). */
@ConfigurationProperties(prefix = "licitawatch.planes")
public record PlanesProperties(Estandar estandar, Premium premium) {
    public record Estandar(int postulacionesMes, List<String> beneficios) {
    }

    public record Premium(BigDecimal precio, int vigenciaDias, int postulacionesMes, List<String> beneficios) {
    }
}
