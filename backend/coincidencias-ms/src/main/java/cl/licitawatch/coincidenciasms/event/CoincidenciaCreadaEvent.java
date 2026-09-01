package cl.licitawatch.coincidenciasms.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoincidenciaCreadaEvent implements Serializable {
    private Long coincidenciaId;
    private Long usuarioId;
    private Long licitacionId;
    private Integer score;
    private String estado;
}
