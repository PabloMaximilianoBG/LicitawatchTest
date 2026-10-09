package cl.licitawatch.notificaciones.bd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_notificacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoNotificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 80)
    private String nombre;
}
