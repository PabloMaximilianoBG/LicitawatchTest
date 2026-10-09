package cl.licitawatch.ventas.bd.dto.request;

import jakarta.validation.constraints.NotBlank;

/** activarSuscripcion = true: la suscripción de la venta pasa a Activa y las demás activas del usuario a Cancelada. */
public record PagoBdRequest(@NotBlank String idTransaccion, @NotBlank String metodo, @NotBlank String estado,
                            boolean activarSuscripcion) {
}
