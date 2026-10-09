package cl.licitawatch.ventas.bs.dto.response;

import lombok.Builder;

import java.time.LocalDate;

/** estadoVisible: el estado del ER o "Pendiente de pago" para un Premium que espera la confirmación de Webpay. */
@Builder
public record SuscripcionResponse(Integer id, Integer usuarioId, String clienteNombre, String clienteEmail, String plan,
                                  String estado, String estadoVisible, LocalDate fechaInicio, LocalDate fechaVencimiento) {
}
