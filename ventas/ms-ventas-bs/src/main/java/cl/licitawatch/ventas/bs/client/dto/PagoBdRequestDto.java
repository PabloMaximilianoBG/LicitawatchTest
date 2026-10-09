package cl.licitawatch.ventas.bs.client.dto;

public record PagoBdRequestDto(String idTransaccion, String metodo, String estado, boolean activarSuscripcion) {
}
