package cl.licitawatch.coincidenciasms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "regla")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Regla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String criterio;

    private Integer peso;
}
