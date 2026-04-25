package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.dto.BillDto;
import vn.edu.uit.is208.salon.dto.CreateBillDetailRequest;
import vn.edu.uit.is208.salon.dto.CreateBillRequest;
import vn.edu.uit.is208.salon.entity.*;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.BillMapper;
import vn.edu.uit.is208.salon.repository.*;
import vn.edu.uit.is208.salon.repository.specifications.BillSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {
    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final BillMapper billMapper;
    private final AppointmentRepository appointmentRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final ProductRepository productRepository;

    public Page<BillDto> getAllBills(
            int page, int size, String paymentStatus, Long customerId, LocalDate startDate, LocalDate endDate
    ) {
        Specification<Bill> spec = Specification
                .where(BillSpecification.hasPaymentStatus(paymentStatus))
                .and(BillSpecification.hasCustomer(customerId))
                .and(BillSpecification.createdAfter(startDate))
                .and(BillSpecification.createdBefore(endDate));

        Pageable pageable = PageRequest.of(page, size, Sort.by("billDate").descending());
        return billRepository.findAll(spec, pageable).map(billMapper::toDto);
    }

    public BillDto getBillById(Long id) {
        return billMapper.toDto(getBill(id));
    }

    @Transactional
    public BillDto createBill(CreateBillRequest request) {
        Customer customer = getCustomer(request.getCustomerId());
        Appointment appointment = Optional.ofNullable(request.getAppointmentId())
                .map(this::getAppointment)
                .orElse(null);

        Bill bill = new Bill();
        bill.setCustomer(customer);
        bill.setAppointment(appointment);

        Map<Long, SalonService> serviceMap = fetchServiceMap(request.getDetails());
        Map<Long, Product> productMap = fetchProductMap(request.getDetails());

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CreateBillDetailRequest d : request.getDetails()) {
            validateMutualExclusive(d.getServiceId(), d.getProductId());

            BillDetail detail = new BillDetail();
            detail.setBill(bill);
            detail.setQuantity(d.getQuantity());

            BigDecimal unitPrice;

            if (d.getServiceId() != null) {
                SalonService service = serviceMap.get(d.getServiceId());
                if (service == null)
                    throw new ResourceNotFoundException("Không tìm thấy dịch vụ với ID: " + d.getServiceId());

                detail.setService(service);
                unitPrice = service.getPrice();
            } else {
                Product product = productMap.get(d.getProductId());
                if (product == null)
                    throw new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + d.getProductId());

                detail.setProduct(product);
                unitPrice = product.getPrice();
            }

            detail.setUnitPrice(unitPrice);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(d.getQuantity()));
            totalAmount = totalAmount.add(lineTotal);

            bill.getDetails().add(detail);
        }

        bill.setTotalAmount(totalAmount);

        return billMapper.toDto(billRepository.save(bill));
    }

    private Bill getBill(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bill với ID: " + billId));
    }

    private Customer getCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + customerId));
    }

    private Appointment getAppointment(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn với ID: " + appointmentId));
    }

    private Map<Long, SalonService> fetchServiceMap(List<CreateBillDetailRequest> details) {
        List<Long> ids = details.stream()
                .map(CreateBillDetailRequest::getServiceId)
                .filter(Objects::nonNull)
                .toList();
        return salonServiceRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(SalonService::getId, s -> s));
    }

    private Map<Long, Product> fetchProductMap(List<CreateBillDetailRequest> details) {
        List<Long> ids = details.stream()
                .map(CreateBillDetailRequest::getProductId)
                .filter(Objects::nonNull)
                .toList();
        return productRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));
    }

    private void validateMutualExclusive(Long serviceId, Long productId) {
        boolean ok = (serviceId != null && productId == null) || (serviceId == null && productId != null);
        if (!ok) {
            throw new BusinessRuleException("Bill detail phải chọn Service hoặc Product (không được chọn cả hai hoặc không chọn)");
        }
    }
}
