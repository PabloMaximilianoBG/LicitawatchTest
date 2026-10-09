package cl.licitawatch.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Página de resultados (evita exponer org.springframework.data.domain.Page en el JSON). */
public record PaginaResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <E, T> PaginaResponse<T> de(Page<E> page, Function<E, T> mapper) {
        return new PaginaResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    public <R> PaginaResponse<R> map(Function<T, R> mapper) {
        return new PaginaResponse<>(content.stream().map(mapper).toList(), page, size, totalElements, totalPages);
    }
}
