package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.constant.PaymentStatus;
import vn.edu.uit.is208.salon.dto.AddRetailProductRequest;
import vn.edu.uit.is208.salon.dto.BillDto;
import vn.edu.uit.is208.salon.dto.CreateAppointmentBillRequest;
import vn.edu.uit.is208.salon.dto.CreateRetailBillRequest;
import vn.edu.uit.is208.salon.entity.*;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.BillMapper;
import vn.edu.uit.is208.salon.repository.*;
import vn.edu.uit.is208.salon.repository.specifications.BillSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final ProductRepository productRepository;
    private final ServiceRecipeRepository serviceRecipeRepository;
    private final BillMapper billMapper;

    public Page<BillDto> getAllBills(
            int page, int size, String paymentStatus, Long customerId, LocalDate startDate, LocalDate endDate) {

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

    private Bill getBill(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy bill với ID: " + billId));
    }

    @Transactional
    public BillDto createRetailBill(CreateRetailBillRequest request) {
        Bill bill = new Bill();
        Map<Long, BigDecimal> inventoryCart = new HashMap<>();

        bill.setCustomer(getCustomer(request.getCustomerId()));
        bill.setTotalAmount(appendRetailProductsToBill(bill, request.getRetailProducts(), inventoryCart));

        validateInventory(inventoryCart);

        return billMapper.toDto(billRepository.save(bill));
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng ID: " + id));
    }

    private BigDecimal appendRetailProductsToBill(
            Bill bill, List<AddRetailProductRequest> requests, Map<Long, BigDecimal> inventoryCart) {

        List<Long> productIds = requests.stream().map(AddRetailProductRequest::getProductId).toList();
        Map<Long, Product> productMap = productRepository.findAllById(productIds)
                .stream().collect(Collectors.toMap(Product::getId, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;

        for (AddRetailProductRequest request : requests) {
            Product product = productMap.get(request.getProductId());
            if (product == null) {
                throw new ResourceNotFoundException("Không tìm thấy sản phẩm ID: " + request.getProductId());
            }
            BigDecimal unitPrice = product.getPrice();
            Long quantity = request.getQuantity();

            BillDetail detail = new BillDetail();
            detail.setBill(bill);
            detail.setProduct(product);
            detail.setUnitPrice(unitPrice);
            detail.setQuantity(quantity);

            bill.getDetails().add(detail);
            total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));

            BigDecimal factor = product.getConversionFactor() != null ? product.getConversionFactor() : BigDecimal.ONE;
            BigDecimal quantityOnBaseUOM = BigDecimal.valueOf(quantity).multiply(factor);
            inventoryCart.merge(product.getId(), quantityOnBaseUOM, BigDecimal::add);
        }

        return total;
    }

    private void validateInventory(Map<Long, BigDecimal> inventoryCart) {
        if (inventoryCart.isEmpty()) return;

        Map<Long, Product> productMap = productRepository.findAllById(inventoryCart.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        inventoryCart.forEach((productId, requiredQuantity) -> {
            Product product = productMap.get(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Không tìm thấy sản phẩm hoặc nguyên liệu ID: " + productId);
            }

            if (product.getQuantityOnHand().compareTo(requiredQuantity) < 0) {
                throw new BusinessRuleException(String.format(
                        "Không đủ tồn kho cho '%s'. Tổng cộng cần: %s, Hiện có: %s",
                        product.getName(), requiredQuantity, product.getQuantityOnHand()));
            }
        });
    }

    @Transactional
    public BillDto createAppointmentBill(Long appointmentId, CreateAppointmentBillRequest request) {
        if (billRepository.existsByAppointment_Id(appointmentId)) {
            throw new BusinessRuleException("Lịch hẹn này đã được lập hóa đơn. Không thể tạo thêm.");
        }

        Bill bill = new Bill();

        Appointment appointment = getAppointment(appointmentId);
        bill.setAppointment(appointment);
        bill.setCustomer(appointment.getCustomer());

        Map<Long, BigDecimal> inventoryCart = new HashMap<>();
        BigDecimal totalAmount = syncServicesFromAppointment(bill, appointment, inventoryCart);

        List<AddRetailProductRequest> retailProducts = request.getRetailProducts();
        if (retailProducts != null && !retailProducts.isEmpty()) {
            totalAmount = totalAmount.add(appendRetailProductsToBill(bill, retailProducts, inventoryCart));
        }

        bill.setTotalAmount(totalAmount);

        validateInventory(inventoryCart);

        return billMapper.toDto(billRepository.save(bill));
    }

    private Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy lịch hẹn ID: " + id));
    }

    private BigDecimal syncServicesFromAppointment(Bill bill, Appointment appointment, Map<Long, BigDecimal> inventoryCart) {
        Set<SalonService> services = appointment.getServices();
        if (services.isEmpty()) return BigDecimal.ZERO;

        List<Long> serviceIds = services.stream().map(SalonService::getId).toList();
        Map<Long, List<ServiceRecipe>> recipesByServiceId = fetchRecipesByServiceId(serviceIds);

        BigDecimal total = BigDecimal.ZERO;

        for (SalonService service : services) {
            BillDetail detail = new BillDetail();
            detail.setBill(bill);
            detail.setService(service);
            detail.setQuantity(1L);
            detail.setUnitPrice(service.getPrice());
            bill.getDetails().add(detail);

            recipesByServiceId.getOrDefault(service.getId(), List.of()).forEach(recipe ->
                    inventoryCart.merge(recipe.getProduct().getId(), recipe.getQuantityConsumed(), BigDecimal::add));

            total = total.add(service.getPrice());
        }

        return total;
    }

    private Map<Long, List<ServiceRecipe>> fetchRecipesByServiceId(List<Long> serviceIds) {
        if (serviceIds.isEmpty()) return Map.of();
        return serviceRecipeRepository.findByService_IdIn(serviceIds).stream()
                .collect(Collectors.groupingBy(recipe -> recipe.getService().getId()));
    }


    @Transactional
    public BillDto addRetailProduct(Long billId, AddRetailProductRequest request) {
        Bill bill = getBill(billId);

        if (bill.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Chỉ có thể thêm món vào hóa đơn đang chờ thanh toán (PENDING)");
        }

        Map<Long, BigDecimal> inventoryCart = new HashMap<>();
        BigDecimal additionalAmount = appendRetailProductsToBill(bill, List.of(request), inventoryCart);
        bill.setTotalAmount(bill.getTotalAmount().add(additionalAmount));

        validateInventory(inventoryCart);

        return billMapper.toDto(billRepository.save(bill));
    }
}
