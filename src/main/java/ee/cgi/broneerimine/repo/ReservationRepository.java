package ee.cgi.broneerimine.repo;

import ee.cgi.broneerimine.domain.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findByStartTimeLessThanAndEndTimeGreaterThan(LocalDateTime to, LocalDateTime from);
}
