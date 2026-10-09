package cl.licitawatch.usuarios.bd.repository;

import cl.licitawatch.usuarios.bd.entity.Administrador;
import cl.licitawatch.usuarios.bd.entity.Licitador;
import cl.licitawatch.usuarios.bd.entity.Pyme;
import cl.licitawatch.usuarios.bd.entity.Usuario;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

/** Filtros del listado de usuarios (Criteria API: parámetros enlazados, sin SQL concatenado). */
public final class UsuarioSpecs {
    private UsuarioSpecs() {
    }

    public static Specification<Usuario> conRol(String rolNombre) {
        return (root, q, cb) -> rolNombre == null || rolNombre.isBlank() ? null
                : cb.equal(cb.lower(root.get("rol").get("nombre")), rolNombre.toLowerCase());
    }

    public static Specification<Usuario> activo(Boolean activo) {
        return (root, q, cb) -> activo == null ? null : cb.equal(root.get("activo"), activo);
    }

    /** Texto libre: email, razón social (licitador/pyme) o nombre del administrador. */
    public static Specification<Usuario> texto(String texto) {
        return (root, query, cb) -> {
            if (texto == null || texto.isBlank()) {
                return null;
            }
            String patron = "%" + texto.trim().toLowerCase() + "%";
            Subquery<Integer> lic = query.subquery(Integer.class);
            var l = lic.from(Licitador.class);
            lic.select(l.get("usuario").get("id")).where(cb.like(cb.lower(l.get("razonSocial")), patron));
            Subquery<Integer> py = query.subquery(Integer.class);
            var p = py.from(Pyme.class);
            py.select(p.get("usuario").get("id")).where(cb.like(cb.lower(p.get("razonSocial")), patron));
            Subquery<Integer> ad = query.subquery(Integer.class);
            var a = ad.from(Administrador.class);
            ad.select(a.get("usuario").get("id")).where(cb.like(cb.lower(a.get("nombre")), patron));
            return cb.or(cb.like(cb.lower(root.get("email")), patron), root.get("id").in(lic), root.get("id").in(py), root.get("id").in(ad));
        };
    }
}
