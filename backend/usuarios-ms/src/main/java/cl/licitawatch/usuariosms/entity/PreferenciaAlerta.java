package cl.licitawatch.usuariosms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "preferencia_alerta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String rubro;

    private BigDecimal monto;

    private String region;

    private String canal;

    private String frecuencia;
}
