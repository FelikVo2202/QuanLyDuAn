package vn.edu.uit.is208.salon.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.uit.is208.salon.constant.PaymentStatus;
import vn.edu.uit.is208.salon.entity.Bill;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {
    boolean existsByAppointment_Id(Long appointmentId);

    @EntityGraph(attributePaths = {"details"})
    Optional<Bill> findById(Long id);

    boolean existsByAppointment_IdAndPaymentStatusNot(Long appointmentId, PaymentStatus paymentStatus);

    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.paymentStatus = 'PAID' AND b.billDate >= :startDateTime AND b.billDate < :endDateTime")
    BigDecimal sumRevenueByDateRange(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    @Query("SELECT b FROM Bill b WHERE b.appointment.id IN :appointmentIds AND b.paymentStatus != 'FAILED'")
    List<Bill> findActiveBillsByAppointmentIds(@Param("appointmentIds") List<Long> appointmentIds);

    @Query("SELECT b FROM Bill b WHERE b.appointment.id = :appointmentId AND b.paymentStatus != 'FAILED'")
    Optional<Bill> findActiveBillByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("SELECT SUM(b.totalAmount) FROM Bill b WHERE b.billDate >= :start AND b.billDate < :end AND b.paymentStatus = :status")
    BigDecimal sumTotalAmountByBillDateBetweenAndPaymentStatus(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") PaymentStatus status);
}
