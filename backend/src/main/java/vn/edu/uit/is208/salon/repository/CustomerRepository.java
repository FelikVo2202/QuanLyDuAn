package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}