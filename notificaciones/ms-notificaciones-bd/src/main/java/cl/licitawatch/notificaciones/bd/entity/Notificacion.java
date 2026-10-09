package cl.licitawatch.notificaciones.bd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** REF a usuario.id de usuarios-bd (no es FK). */
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tipo_notificacion_id", nullable = false)
    private TipoNotificacion tipoNotificacion;

    @Column(name = "canal", nullable = false, length = 20)
    private String canal;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
