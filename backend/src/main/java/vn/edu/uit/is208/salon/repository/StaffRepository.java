package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.Staff;

public interface StaffRepository extends JpaRepository<Staff, Long> {
    boolean existsByUsername(String username);
}