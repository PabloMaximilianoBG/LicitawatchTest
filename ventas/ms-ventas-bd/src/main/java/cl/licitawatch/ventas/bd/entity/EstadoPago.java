package cl.licitawatch.ventas.bd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoPago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
