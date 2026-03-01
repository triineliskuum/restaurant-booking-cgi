package ee.cgi.broneerimine.repo;

import ee.cgi.broneerimine.domain.TableEntity;
import ee.cgi.broneerimine.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    List<TableEntity> findByZone(Zone zone);
}
