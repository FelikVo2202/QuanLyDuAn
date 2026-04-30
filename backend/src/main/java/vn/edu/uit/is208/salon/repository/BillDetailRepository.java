package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.uit.is208.salon.constant.PaymentStatus;
import vn.edu.uit.is208.salon.entity.BillDetail;

public interface BillDetailRepository extends JpaRepository<BillDetail, Long> {
    boolean existsByProductId(Long productId);

    boolean existsByProductIdAndBillPaymentStatus(Long productId, PaymentStatus paymentStatus);
}
