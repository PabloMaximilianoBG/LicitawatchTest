package cl.licitawatch.licitaciones.bd.dto.response;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record PostulacionBdResponse(Integer id, Integer licitacionId, String licitacionTitulo, Integer licitadorId,
                                    String licitacionEstado, LocalDate fechaCierre, Integer pymeId, String mensaje,
                                    LocalDate fechaPostulacion, String estado, LocalDateTime updatedAt) {
}
