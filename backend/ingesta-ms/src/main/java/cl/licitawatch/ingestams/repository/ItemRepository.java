package cl.licitawatch.ingestams.repository;

import cl.licitawatch.ingestams.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
