package cl.licitawatch.licitaciones.bd.repository;

import cl.licitawatch.licitaciones.bd.entity.Licitacion;
import cl.licitawatch.licitaciones.bd.entity.Postulacion;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Filtros de búsqueda (Criteria API: parámetros enlazados, sin SQL concatenado). */
public final class LicitacionSpecs {
    private LicitacionSpecs() {
    }

    public static Specification<Licitacion> texto(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return null;
            }
            String patron = "%" + q.trim().toLowerCase() + "%";
            return cb.or(cb.like(cb.lower(root.get("titulo")), patron), cb.like(cb.lower(root.get("descripcion")), patron));
        };
    }

    public static Specification<Licitacion> igual(String campo, Integer valor) {
        return (root, query, cb) -> valor == null ? null : cb.equal(root.get(campo), valor);
    }

    public static Specification<Licitacion> estado(String estado) {
        return (root, query, cb) -> estado == null || estado.isBlank() ? null
                : cb.equal(cb.lower(root.get("estadoLicitacion").get("nombre")), estado.toLowerCase());
    }

    /** El presupuesto de la licitación debe alcanzar el mínimo buscado (usa presupuesto_max, o presupuesto_min si no hay máximo). */
    public static Specification<Licitacion> presupuestoDesde(BigDecimal min) {
        return (root, query, cb) -> min == null ? null
                : cb.greaterThanOrEqualTo(cb.coalesce(root.get("presupuestoMax"), root.get("presupuestoMin")), min);
    }

    public static Specification<Licitacion> presupuestoHasta(BigDecimal max) {
        return (root, query, cb) -> max == null ? null
                : cb.lessThanOrEqualTo(cb.coalesce(root.get("presupuestoMin"), root.get("presupuestoMax")), max);
    }

    public static Specification<Licitacion> vigenteDesde(LocalDate hoy) {
        return (root, query, cb) -> hoy == null ? null : cb.greaterThanOrEqualTo(root.get("fechaCierre"), hoy);
    }

    /** Excluye las licitaciones que ya alcanzaron max_postulantes. */
    public static Specification<Licitacion> conCupo(Boolean conCupo) {
        return (root, query, cb) -> {
            if (!Boolean.TRUE.equals(conCupo)) {
                return null;
            }
            Subquery<Long> sub = query.subquery(Long.class);
            var p = sub.from(Postulacion.class);
            sub.select(cb.count(p)).where(cb.equal(p.get("licitacion"), root));
            return cb.or(cb.isNull(root.get("maxPostulantes")), cb.lessThan(sub, root.get("maxPostulantes").as(Long.class)));
        };
    }
}
