package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.Bill;

public interface BillRepository extends JpaRepository<Bill, Long> {
}
