package cl.licitawatch.licitaciones.bs.dto.request;

import jakarta.validation.constraints.Size;

public record PostularRequest(@Size(max = 2000, message = "El mensaje admite hasta 2000 caracteres") String mensaje) {
}
