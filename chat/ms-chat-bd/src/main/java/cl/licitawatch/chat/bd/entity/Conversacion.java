package cl.licitawatch.chat.bd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** REF a postulacion.id de licitaciones-bd (una conversación por postulación). */
    @Column(name = "postulacion_id", nullable = false, unique = true)
    private Integer postulacionId;

    /** REF a licitador.id de usuarios-bd (copia controlada para verificar el acceso). */
    @Column(name = "licitador_id", nullable = false)
    private Integer licitadorId;

    /** REF a pyme.id de usuarios-bd. */
    @Column(name = "pyme_id", nullable = false)
    private Integer pymeId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
