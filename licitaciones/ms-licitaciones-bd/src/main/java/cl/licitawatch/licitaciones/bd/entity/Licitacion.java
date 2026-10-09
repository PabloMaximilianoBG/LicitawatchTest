package cl.licitawatch.licitaciones.bd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "licitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Licitacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /** REF a licitador.id de usuarios-bd (no es FK). */
    @Column(name = "licitador_id", nullable = false)
    private Integer licitadorId;

    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    /** REF a rubro.id de usuarios-bd. */
    @Column(name = "rubro_id", nullable = false)
    private Integer rubroId;

    /** REF a region.id de usuarios-bd. */
    @Column(name = "region_id", nullable = false)
    private Integer regionId;

    @Column(name = "presupuesto_min", precision = 15, scale = 2)
    private BigDecimal presupuestoMin;

    @Column(name = "presupuesto_max", precision = 15, scale = 2)
    private BigDecimal presupuestoMax;

    @Column(name = "max_postulantes")
    private Integer maxPostulantes;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "archivo_url", length = 500)
    private String archivoUrl;

    @Column(name = "archivo_nombre", length = 255)
    private String archivoNombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_archivo_id")
    private TipoArchivo tipoArchivo;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "estado_licitacion_id", nullable = false)
    private EstadoLicitacion estadoLicitacion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
