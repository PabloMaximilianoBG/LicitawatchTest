package cl.licitawatch.licitaciones.bff.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Postulación con los datos de la Pyme (para el Licitador) y de la licitación (para la Pyme). */
@Builder
public record PostulacionResponse(Integer id, Integer licitacionId, String licitacionTitulo, String licitacionEstado,
                                  LocalDate fechaCierre, Integer licitadorId, String licitadorNombre,
                                  Integer pymeId, Integer pymeUsuarioId, String pymeRazonSocial, String pymeRut,
                                  String pymeRubro, String pymeCiudad, String pymeRegion, String pymeTamano,
                                  String pymeContacto, String pymeEmailContacto, String pymeTelefono, boolean pymePremium,
                                  String mensaje, LocalDate fechaPostulacion, String estado, LocalDateTime updatedAt,
                                  boolean chatDisponible) {
}
