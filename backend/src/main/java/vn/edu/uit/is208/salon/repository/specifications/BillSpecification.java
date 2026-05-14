package vn.edu.uit.is208.salon.repository.specifications;

import org.springframework.data.jpa.domain.Specification;
import vn.edu.uit.is208.salon.entity.Bill;

import java.time.LocalDate;
import java.time.LocalTime;

public class BillSpecification {

    public static Specification<Bill> hasPaymentStatus(String paymentStatus) {
        return (root, query, criteriaBuilder) -> {
            if (paymentStatus == null || paymentStatus.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("paymentStatus"), paymentStatus);
        };
    }

    public static Specification<Bill> hasCustomer(Long customerId) {
        return (root, query, criteriaBuilder) -> {
            if (customerId == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("customer").get("id"), customerId);
        };
    }

    public static Specification<Bill> createdAfter(LocalDate startDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("billDate"), startDate.atStartOfDay());
        };
    }

    public static Specification<Bill> createdBefore(LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (endDate == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("billDate"), endDate.atTime(LocalTime.MAX));
        };
    }
}