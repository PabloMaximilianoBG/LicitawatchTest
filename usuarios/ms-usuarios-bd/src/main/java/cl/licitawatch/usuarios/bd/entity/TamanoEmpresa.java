package cl.licitawatch.usuarios.bd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tamano_empresa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TamanoEmpresa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
}
