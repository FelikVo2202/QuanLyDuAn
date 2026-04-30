package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.constant.InventoryTransactionType;
import vn.edu.uit.is208.salon.dto.InventoryLedgerResponse;
import vn.edu.uit.is208.salon.dto.StockUpdateRequest;
import vn.edu.uit.is208.salon.entity.InventoryLedger;
import vn.edu.uit.is208.salon.entity.Product;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.InventoryLedgerMapper;
import vn.edu.uit.is208.salon.repository.InventoryLedgerRepository;
import vn.edu.uit.is208.salon.repository.ProductRepository;
import vn.edu.uit.is208.salon.repository.specifications.InventoryLedgerSpecification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductRepository productRepository;
    private final InventoryLedgerRepository inventoryLedgerRepository;
    private final InventoryLedgerMapper inventoryLedgerMapper;

    @Transactional(readOnly = true)
    public Page<InventoryLedgerResponse> getAll(
            Long productId,
            InventoryTransactionType transactionType,
            Long referenceId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new BusinessRuleException("Ngày bắt đầu (startDate) phải nhỏ hơn hoặc bằng ngày kết thúc (endDate)");
        }

        Pageable safePageable = pageable;
        if (pageable.getPageSize() > 100) {
            safePageable = PageRequest.of(pageable.getPageNumber(), 100, pageable.getSort());
        }

        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;

        if (startDate != null) {
            startDateTime = startDate.atStartOfDay();
        }
        if (endDate != null) {
            endDateTime = endDate.atTime(LocalTime.MAX);
        }
        if (startDate != null && endDate == null) {
            endDateTime = LocalDateTime.now();
        }

        Specification<InventoryLedger> spec = InventoryLedgerSpecification.withFilters(
                productId,
                transactionType,
                referenceId,
                startDateTime,
                endDateTime
        );

        return inventoryLedgerRepository.findAll(spec, safePageable).map(inventoryLedgerMapper::toResponse);
    }

    @Transactional
    public void addStock(StockUpdateRequest request) {
        Product product = productRepository.findById(request.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm"));

        product.setQuantityOnHand(product.getQuantityOnHand().add(request.getQuantity()));
        productRepository.save(product);
    }
}