package cl.licitawatch.ingestams.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicitacionCreadaEvent implements Serializable {
    private Long licitacionId;
    private String codigo;
    private String rubro;
    private String region;
    private BigDecimal monto;
    private String estado;
    private LocalDate fechaCierre;
}
