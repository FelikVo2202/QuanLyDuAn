package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.edu.uit.is208.salon.constant.PaymentStatus;
import vn.edu.uit.is208.salon.entity.Bill;

import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {
    boolean existsByAppointment_Id(Long appointmentId);

    @EntityGraph(attributePaths = {"details"})
    Optional<Bill> findById(Long id);

    boolean existsByAppointment_IdAndPaymentStatusNot(Long appointmentId, PaymentStatus paymentStatus);
}
