package cl.licitawatch.usuarios.bd.repository;

import cl.licitawatch.usuarios.bd.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<Region, Integer> {
}
