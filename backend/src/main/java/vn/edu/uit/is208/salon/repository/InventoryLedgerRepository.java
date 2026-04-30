package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.edu.uit.is208.salon.entity.InventoryLedger;

public interface InventoryLedgerRepository extends JpaRepository<InventoryLedger, Long>, JpaSpecificationExecutor<InventoryLedger> {
    boolean existsByProduct_Id(Long productId);
}
