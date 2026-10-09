package cl.licitawatch.licitaciones.bs.client.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record PostulacionBdDto(Integer id, Integer licitacionId, String licitacionTitulo, Integer licitadorId,
                               String licitacionEstado, LocalDate fechaCierre, Integer pymeId, String mensaje,
                               LocalDate fechaPostulacion, String estado, LocalDateTime updatedAt) {
}
