package cl.licitawatch.notificaciones.bs.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SoporteRequest(@NotBlank(message = "El asunto es obligatorio") @Size(max = 150) String asunto,
                             @NotBlank(message = "Describe tu consulta") @Size(max = 4000) String mensaje) {
}
