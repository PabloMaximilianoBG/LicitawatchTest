package cl.licitawatch.ventas.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * "token" (el token_ws de Webpay) no esta en el modelo de datos minimo de la
 * seccion 3.3, pero es imprescindible para poder confirmar la transaccion
 * despues del redirect (Transbank identifica la transaccion por su token, no
 * por nuestro id de venta) - se agrega como detalle de implementacion.
 * "id_transaccion" guarda el authorizationCode que entrega Transbank al
 * confirmar, que es el campo que sí pedia el modelo original.
 */
@Entity
@Table(name = "pago")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_id", nullable = false, unique = true)
    private Venta venta;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "id_transaccion", length = 64)
    private String idTransaccion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado = EstadoPago.PENDIENTE;
}
