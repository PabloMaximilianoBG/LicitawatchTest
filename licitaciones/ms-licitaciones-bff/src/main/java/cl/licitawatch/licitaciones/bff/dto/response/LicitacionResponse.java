package cl.licitawatch.licitaciones.bff.dto.response;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder(toBuilder = true)
public record LicitacionResponse(Integer id, Integer licitadorId, String licitadorNombre, String titulo, String descripcion,
                                 Integer rubroId, String rubroNombre, Integer regionId, String regionNombre,
                                 BigDecimal presupuestoMin, BigDecimal presupuestoMax, Integer maxPostulantes,
                                 String imagenUrl, String archivoUrl, String archivoNombre, String tipoArchivo,
                                 LocalDate fechaCierre, String estado, LocalDateTime createdAt, long cantidadPostulaciones,
                                 Integer cuposDisponibles, long diasParaCierre, boolean disponibleParaPostular,
                                 Integer miPostulacionId, String miPostulacionEstado) {
}
