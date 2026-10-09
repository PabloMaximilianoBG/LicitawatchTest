package cl.licitawatch.chat.bd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversacion conversacion;

    /** REF a usuario.id de usuarios-bd. */
    @Column(name = "emisor_id", nullable = false)
    private Integer emisorId;

    @Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "enviado_at", nullable = false)
    private LocalDateTime enviadoAt;

    @Column(name = "leido", nullable = false)
    private Boolean leido;
}
