package cl.licitawatch.coincidenciasms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Copia local y desnormalizada de PreferenciaAlerta (usuarios-ms), llenada solo a
 * partir del evento "preferencia.actualizada".
 */
@Entity
@Table(name = "preferencia_local")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaLocal {

    @Id
    private Long id; // mismo id que PreferenciaAlerta en usuarios-ms

    private Long usuarioId;
    private String rubro;
    private String region;
    private BigDecimal monto;
}
