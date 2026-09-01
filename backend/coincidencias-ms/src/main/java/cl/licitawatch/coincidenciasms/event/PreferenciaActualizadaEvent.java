package cl.licitawatch.coincidenciasms.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaActualizadaEvent implements Serializable {
    private Long preferenciaId;
    private Long usuarioId;
    private String rubro;
    private String region;
    private BigDecimal monto;
    private String canal;
    private String frecuencia;
}
