package cl.licitawatch.licitaciones.bd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_archivo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoArchivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
