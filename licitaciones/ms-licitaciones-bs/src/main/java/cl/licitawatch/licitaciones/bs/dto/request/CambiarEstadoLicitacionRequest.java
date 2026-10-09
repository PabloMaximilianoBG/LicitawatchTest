package cl.licitawatch.licitaciones.bs.dto.request;

import jakarta.validation.constraints.NotBlank;

/** estado = Abierta | Cerrada (Adjudicada solo se alcanza aprobando una postulación). */
public record CambiarEstadoLicitacionRequest(@NotBlank String estado) {
}
