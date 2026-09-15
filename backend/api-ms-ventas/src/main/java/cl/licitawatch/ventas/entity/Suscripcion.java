package cl.licitawatch.ventas.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * usuario_id referencia a usuario.id en API Usuarios (sin FK fisica, igual
 * que empresa_id/cliente_id en API Licitaciones).
 *
 * El modelo de datos solo define 2 estados (ACTIVA/VENCIDA), sin un tercer
 * estado "pendiente". Por eso una suscripcion se crea en VENCIDA al iniciar
 * una compra (fecha_inicio/fecha_vencimiento en null) y solo pasa a ACTIVA
 * cuando MS-Ventas confirma el pago (ver VentaService). VENCIDA cubre tanto
 * "nunca se activo" como "se activo y ya expiro".
 */
@Entity
@Table(name = "suscripcion")
@Getter
@Setter
@NoArgsConstructor
public class Suscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanSuscripcion plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EstadoSuscripcion estado = EstadoSuscripcion.VENCIDA;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn = LocalDateTime.now();
}
