package cl.licitawatch.licitaciones.repository;

import cl.licitawatch.licitaciones.entity.EstadoLicitacion;
import cl.licitawatch.licitaciones.entity.Licitacion;
import org.springframework.data.jpa.domain.Specification;

public final class LicitacionSpecifications {

    private LicitacionSpecifications() {
    }

    public static Specification<Licitacion> conEstado(EstadoLicitacion estado) {
        return (root, query, cb) -> estado == null ? null : cb.equal(root.get("estado"), estado);
    }

    public static Specification<Licitacion> conRubro(String rubro) {
        return (root, query, cb) -> (rubro == null || rubro.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("rubro")), rubro.toLowerCase());
    }

    public static Specification<Licitacion> conRegion(String region) {
        return (root, query, cb) -> (region == null || region.isBlank())
                ? null
                : cb.equal(cb.lower(root.get("region")), region.toLowerCase());
    }
}
