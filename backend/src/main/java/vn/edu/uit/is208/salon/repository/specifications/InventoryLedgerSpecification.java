package vn.edu.uit.is208.salon.repository.specifications;

import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import vn.edu.uit.is208.salon.constant.InventoryTransactionType;
import vn.edu.uit.is208.salon.entity.InventoryLedger;

import java.time.LocalDateTime;

public final class InventoryLedgerSpecification {

    private InventoryLedgerSpecification() {}

    public static Specification<InventoryLedger> withFilters(
            Long productId,
            InventoryTransactionType transactionType,
            Long referenceId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    ) {
        return Specification
                .where(fetchProduct())
                .and(hasProductId(productId))
                .and(hasTransactionType(transactionType))
                .and(hasReferenceId(referenceId))
                .and(transactionDateAfter(startDateTime))
                .and(transactionDateBefore(endDateTime));
    }

    private static Specification<InventoryLedger> fetchProduct() {
        return (root, query, cb) -> {
            if (Long.class != query.getResultType() && long.class != query.getResultType()) {
                root.fetch("product", JoinType.LEFT);
            }
            return cb.conjunction();
        };
    }

    public static Specification<InventoryLedger> hasProductId(Long productId) {
        return (root, query, cb) -> {
            if (productId == null) return null;
            return cb.equal(root.get("product").get("id"), productId);
        };
    }

    public static Specification<InventoryLedger> hasTransactionType(InventoryTransactionType transactionType) {
        return (root, query, cb) -> {
            if (transactionType == null) return null;
            return cb.equal(root.get("transactionType"), transactionType);
        };
    }

    public static Specification<InventoryLedger> hasReferenceId(Long referenceId) {
        return (root, query, cb) -> {
            if (referenceId == null) return null;
            return cb.equal(root.get("referenceId"), referenceId);
        };
    }

    public static Specification<InventoryLedger> transactionDateAfter(LocalDateTime startDateTime) {
        return (root, query, cb) -> {
            if (startDateTime == null) return null;
            return cb.greaterThanOrEqualTo(root.get("transactionDate"), startDateTime);
        };
    }

    public static Specification<InventoryLedger> transactionDateBefore(LocalDateTime endDateTime) {
        return (root, query, cb) -> {
            if (endDateTime == null) return null;
            return cb.lessThanOrEqualTo(root.get("transactionDate"), endDateTime);
        };
    }
}
