package cl.licitawatch.licitaciones.bd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "postulacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Postulacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "licitacion_id", nullable = false)
    private Licitacion licitacion;

    /** REF a pyme.id de usuarios-bd (no es FK). */
    @Column(name = "pyme_id", nullable = false)
    private Integer pymeId;

    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_postulacion", nullable = false)
    private LocalDate fechaPostulacion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "estado_postulacion_id", nullable = false)
    private EstadoPostulacion estadoPostulacion;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
