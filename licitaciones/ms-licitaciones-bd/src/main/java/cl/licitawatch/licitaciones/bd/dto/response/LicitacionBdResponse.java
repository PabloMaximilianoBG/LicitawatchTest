package cl.licitawatch.licitaciones.bd.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record LicitacionBdResponse(Integer id, Integer licitadorId, String titulo, String descripcion, Integer rubroId,
                                   Integer regionId, BigDecimal presupuestoMin, BigDecimal presupuestoMax,
                                   Integer maxPostulantes, String imagenUrl, String archivoUrl, String archivoNombre,
                                   String tipoArchivo, LocalDate fechaCierre, String estado, LocalDateTime createdAt,
                                   long cantidadPostulaciones) {
}
