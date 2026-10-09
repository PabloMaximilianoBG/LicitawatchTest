package cl.licitawatch.ventas.bd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suscripcion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** REF a usuario.id de usuarios-bd (no es FK). */
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanSuscripcion plan;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "estado_suscripcion_id", nullable = false)
    private EstadoSuscripcion estadoSuscripcion;
}
