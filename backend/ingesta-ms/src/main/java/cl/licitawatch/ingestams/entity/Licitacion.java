package cl.licitawatch.ingestams.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "licitacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Licitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    private String rubro;

    private BigDecimal monto;

    private String region;

    private String estado;

    private LocalDate fechaCierre;

    @OneToMany(mappedBy = "licitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Item> items = new ArrayList<>();
}
