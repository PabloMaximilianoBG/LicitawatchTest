package cl.licitawatch.licitaciones.bs.dto.request;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BusquedaRequest(String q, Integer rubroId, Integer regionId, BigDecimal presupuestoMin, BigDecimal presupuestoMax,
                              String estado, String orden, int page, int size) {
}
