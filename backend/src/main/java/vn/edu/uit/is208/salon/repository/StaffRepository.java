package vn.edu.uit.is208.salon.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.uit.is208.salon.constant.StaffRole;
import vn.edu.uit.is208.salon.entity.Staff;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    boolean existsByUsername(String username);

    Optional<Staff> findByUsername(String username);

    List<Staff> findByRole(StaffRole role);

    @Query("""
        SELECT s.firstName, s.lastName, s.role, COUNT(a.id) as serviceCount
        FROM Staff s
        LEFT JOIN s.appointments a
        WHERE a.status IN ('DONE', 'PAID') OR a.status IS NULL
        GROUP BY s.id, s.firstName, s.lastName, s.role
        ORDER BY serviceCount DESC
    """)
    List<Object[]> findStaffPerformance(Pageable pageable);
}
