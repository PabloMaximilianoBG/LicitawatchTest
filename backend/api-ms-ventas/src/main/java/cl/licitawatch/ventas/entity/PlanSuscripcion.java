package cl.licitawatch.ventas.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "plan_suscripcion")
@Getter
@Setter
@NoArgsConstructor
public class PlanSuscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private NombrePlan nombre;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "limite_publicaciones_mes")
    private Integer limitePublicacionesMes;

    @Column(name = "limite_postulaciones_mes")
    private Integer limitePostulacionesMes;

    @Column(name = "soporte_prioritario", nullable = false)
    private boolean soportePrioritario;

    @Column(name = "notificaciones_automaticas", nullable = false)
    private boolean notificacionesAutomaticas;
}
