package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.SalonService;

public interface SalonServiceRepository extends JpaRepository<SalonService, Long> {
}