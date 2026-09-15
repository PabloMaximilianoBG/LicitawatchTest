package cl.licitawatch.licitaciones.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * empresa_id referencia a usuario.id en API Usuarios (el mismo id que viaja
 * en el "sub" del JWT) - sin FK fisica, se valida por API (seccion 2 del
 * diseno: "cero acceso directo entre bases de datos de distintos servicios").
 */
@Entity
@Table(name = "licitacion")
@Getter
@Setter
@NoArgsConstructor
public class Licitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 100)
    private String rubro;

    @Column(name = "monto_estimado", precision = 14, scale = 2)
    private BigDecimal montoEstimado;

    @Column(nullable = false, length = 100)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoLicitacion estado = EstadoLicitacion.PUBLICADA;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
