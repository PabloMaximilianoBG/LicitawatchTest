package cl.licitawatch.usuarios.bd.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pyme")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pyme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @OneToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(name = "rut", nullable = false, unique = true, length = 12)
    private String rut;

    @Column(name = "nombre_contacto", nullable = false, length = 150)
    private String nombreContacto;

    @Column(name = "email_contacto", nullable = false, length = 255)
    private String emailContacto;

    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "rubro_id", nullable = false)
    private Rubro rubro;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ciudad_id", nullable = false)
    private Ciudad ciudad;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "tamano_empresa_id", nullable = false)
    private TamanoEmpresa tamanoEmpresa;

    @Column(name = "descripcion_empresa", nullable = false, columnDefinition = "TEXT")
    private String descripcionEmpresa;

    @Column(name = "sitio_web", length = 255)
    private String sitioWeb;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
