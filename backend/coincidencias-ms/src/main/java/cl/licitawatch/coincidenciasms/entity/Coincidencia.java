package cl.licitawatch.coincidenciasms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coincidencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Coincidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Referencia por ID a ingesta-ms, sin FK real entre bases de datos.
    @Column(nullable = false)
    private Long licitacionId;

    // Referencia por ID a usuarios-ms, sin FK real entre bases de datos.
    @Column(nullable = false)
    private Long usuarioId;

    private Integer score;

    private String estado;
}
