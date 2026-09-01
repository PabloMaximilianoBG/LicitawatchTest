package cl.licitawatch.coincidenciasms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Copia local y desnormalizada de los campos de Licitacion (ingesta-ms) que este
 * servicio necesita para calcular el score. Se llena solo a partir del evento
 * "licitacion.creada" — coincidencias-ms nunca consulta la BD de ingesta-ms directamente.
 */
@Entity
@Table(name = "licitacion_local")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicitacionLocal {

    @Id
    private Long id; // mismo id que Licitacion en ingesta-ms

    private String codigo;
    private String rubro;
    private String region;
    private BigDecimal monto;
    private String estado;
}
