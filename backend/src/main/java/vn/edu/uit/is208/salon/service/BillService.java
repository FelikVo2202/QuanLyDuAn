package vn.edu.uit.is208.salon.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.uit.is208.salon.constant.AppointmentStatus;
import vn.edu.uit.is208.salon.constant.PaymentStatus;
import vn.edu.uit.is208.salon.constant.ProductType;
import vn.edu.uit.is208.salon.dto.AddRetailProductRequest;
import vn.edu.uit.is208.salon.dto.BillDto;
import vn.edu.uit.is208.salon.dto.CreateAppointmentBillRequest;
import vn.edu.uit.is208.salon.dto.CreateRetailBillRequest;
import vn.edu.uit.is208.salon.entity.*;
import vn.edu.uit.is208.salon.exception.BusinessRuleException;
import vn.edu.uit.is208.salon.exception.ResourceNotFoundException;
import vn.edu.uit.is208.salon.mapper.BillMapper;
import vn.edu.uit.is208.salon.projection.ProductSummary;
import vn.edu.uit.is208.salon.repository.*;
import vn.edu.uit.is208.salon.repository.specifications.BillSpecification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with ID: " + billId));
    }

    @Transactional
    public BillDto createRetailBill(CreateRetailBillRequest request) {
        Bill bill = new Bill();
        Map<Long, BigDecimal> inventoryCart = new HashMap<>();

        bill.setCustomer(getCustomer(request.getCustomerId()));
        bill.setTotalAmount(appendRetailProductsToBill(bill, request.getRetailProducts(), inventoryCart));

        reserveInventory(inventoryCart);

        return billMapper.toDto(billRepository.save(bill));
    }

    private Customer getCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + id));
    }

    private BigDecimal appendRetailProductsToBill(
            Bill bill, List<AddRetailProductRequest> requests, Map<Long, BigDecimal> inventoryCart) {

        List<Long> productIds = requests.stream().map(AddRetailProductRequest::getProductId).toList();
        Map<Long, ProductSummary> productSummaryMap = productRepository.getProductSummaryByIds(productIds)
                .stream().collect(Collectors.toMap(ProductSummary::getId, Function.identity()));

        BigDecimal total = BigDecimal.ZERO;

        for (AddRetailProductRequest request : requests) {
            ProductSummary productSummary = productSummaryMap.get(request.getProductId());
            if (productSummary == null) {
                throw new ResourceNotFoundException("Product not found with ID: " + request.getProductId());
            }

            if (productSummary.getProductType() == ProductType.PROFESSIONAL) {
                throw new BusinessRuleException("Product with id: " + productSummary.getId() +
                        " is an internal ingredient and cannot be sold as retail");
            }

            mergeOrAddDetail(bill, request, productSummary);
            total = total.add(productSummary.getPrice().
                    multiply(BigDecimal.valueOf(request.getQuantity())));

            BigDecimal factor = productSummary.getConversionFactor() != null ? productSummary.getConversionFactor() : BigDecimal.ONE;
            BigDecimal quantityOnBaseUOM = BigDecimal.valueOf(request.getQuantity()).multiply(factor);
            inventoryCart.merge(productSummary.getId(), quantityOnBaseUOM, BigDecimal::add);
        }

        return total;
    }

    private void mergeOrAddDetail(Bill bill, AddRetailProductRequest request, ProductSummary productSummary) {
        bill.getDetails().stream()
                .filter(d -> isSameProduct(d, request.getProductId()))
                .filter(d -> d.getUnitPrice().compareTo(productSummary.getPrice()) == 0)
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.getQuantity()),
                        () -> {
                            BillDetail detail = new BillDetail();
                            detail.setBill(bill);
                            detail.setProduct(productRepository.getReferenceById(productSummary.getId()));
                            detail.setUnitPrice(productSummary.getPrice());
                            detail.setQuantity(request.getQuantity());

                            bill.getDetails().add(detail);
                        }
                );
    }

    private boolean isSameProduct(BillDetail detail, Long productId) {
        return detail.getProduct() != null && Objects.equals(detail.getProduct().getId(), productId);
    }

    private void reserveInventory(Map<Long, BigDecimal> inventoryCart) {
        if (inventoryCart.isEmpty()) return;

        Map<Long, Product> productMap = productRepository.findByIdsWithLock(inventoryCart.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        inventoryCart.forEach((productId, requiredQuantity) -> {
            Product product = productMap.get(productId);
            if (product == null) {
                throw new ResourceNotFoundException("Product or ingredient not found with ID: " + productId);
            }

            if (product.getQuantityOnHand().compareTo(requiredQuantity) < 0) {
                throw new BusinessRuleException(String.format(
                        "Insufficient inventory for '%s'. Required: %s, Available: %s",
                        product.getName(), requiredQuantity, product.getQuantityOnHand()));
            }

            product.setQuantityOnHand(product.getQuantityOnHand().subtract(requiredQuantity));
        });
    }

    @Transactional
    public BillDto createAppointmentBill(Long appointmentId, CreateAppointmentBillRequest request) {
        if (billRepository.existsByAppointment_IdAndPaymentStatusNot(appointmentId, PaymentStatus.FAILED)) {
            throw new BusinessRuleException("This appointment already has an active bill. Cannot create another bill.");
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

        reserveInventory(inventoryCart);

        return billMapper.toDto(billRepository.save(bill));
    }

    private Appointment getAppointment(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with ID: " + id));
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
        ensureBillIsPending(bill);

        Map<Long, BigDecimal> inventoryCart = new HashMap<>();
        BigDecimal additionalAmount = appendRetailProductsToBill(bill, List.of(request), inventoryCart);
        bill.setTotalAmount(bill.getTotalAmount().add(additionalAmount));

        reserveInventory(inventoryCart);

        return billMapper.toDto(billRepository.save(bill));
    }

    private void ensureBillIsPending(Bill bill) {
        if (bill.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BusinessRuleException("Action denied: Bill is not in PENDING status");
        }
    }

    @Transactional
    public BillDto removeRetailProduct(Long billId, Long detailId) {
        Bill bill = getBill(billId);
        ensureBillIsPending(bill);

        BillDetail targetDetail = findRetailDetailOrThrow(bill, detailId);
        Long productId = targetDetail.getProduct().getId();
        Product product = getSingleLockedProductOrThrow(productId);

        BigDecimal qty = BigDecimal.valueOf(targetDetail.getQuantity());
        BigDecimal factor = product.getConversionFactor() != null ? product.getConversionFactor() : BigDecimal.ONE;
        product.setQuantityOnHand(product.getQuantityOnHand().add(qty.multiply(factor)));

        BigDecimal itemTotal = targetDetail.getUnitPrice().multiply(qty);
        bill.setTotalAmount(bill.getTotalAmount().subtract(itemTotal));

        bill.getDetails().remove(targetDetail);

        productRepository.save(product);
        return billMapper.toDto(billRepository.save(bill));
    }

    private BillDetail findRetailDetailOrThrow(Bill bill, Long detailId) {
        BillDetail targetDetail = bill.getDetails().stream()
                .filter(d -> d.getId() != null && d.getId().equals(detailId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Detail line not found with ID: " + detailId + " in this bill"));

        if (targetDetail.getProduct() == null) {
            throw new BusinessRuleException(
                    "You cannot edit or remove services directly on a bill. Please update them via the appointment"
            );
        }

        return targetDetail;
    }

    private Product getSingleLockedProductOrThrow(Long productId) {
        return productRepository.findByIdsWithLock(Set.of(productId)).stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
    }

    @Transactional
    public BillDto updateRetailProductQuantity(Long billId, Long detailId, Long newQuantity) {
        Bill bill = getBill(billId);
        ensureBillIsPending(bill);

        BillDetail targetDetail = findRetailDetailOrThrow(bill, detailId);
        Product product = getSingleLockedProductOrThrow(targetDetail.getProduct().getId());

        Long oldQuantity = targetDetail.getQuantity();
        BigDecimal diffInventory = getDiffInventory(newQuantity, oldQuantity, product);

        product.setQuantityOnHand(product.getQuantityOnHand().subtract(diffInventory));

        BigDecimal oldItemTotal = targetDetail.getUnitPrice().multiply(BigDecimal.valueOf(oldQuantity));
        BigDecimal newItemTotal = targetDetail.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity));

        targetDetail.setQuantity(newQuantity);
        bill.setTotalAmount(bill.getTotalAmount().subtract(oldItemTotal).add(newItemTotal));

        productRepository.save(product);
        return billMapper.toDto(billRepository.save(bill));
    }

    private static @NonNull BigDecimal getDiffInventory(Long newQuantity, Long oldQuantity, Product product) {
        BigDecimal diffQty = BigDecimal.valueOf(newQuantity - oldQuantity);

        BigDecimal factor = product.getConversionFactor() != null ? product.getConversionFactor() : BigDecimal.ONE;
        BigDecimal diffInventory = diffQty.multiply(factor);

        if (diffQty.compareTo(BigDecimal.ZERO) > 0) {
            if (product.getQuantityOnHand().compareTo(diffInventory) < 0) {
                throw new BusinessRuleException("Insufficient inventory. Required: " + diffInventory + ". Available: " + product.getQuantityOnHand());
            }
        }
        return diffInventory;
    }

    @Transactional
    public BillDto payBill(Long billId) {
        Bill bill = getBill(billId);
        ensureBillIsPending(bill);
        bill.setPaymentStatus(PaymentStatus.PAID);

        if (bill.getAppointment().getId() != null) {
            Appointment appointment = getAppointment(bill.getAppointment().getId());
            appointment.setStatus(AppointmentStatus.PAID);
        }

        return billMapper.toDto(billRepository.save(bill));
    }

    @Transactional
    public BillDto cancelBill(Long billId) {
        Bill bill = getBill(billId);
        ensureBillIsPending(bill);

        restoreInventoryForCancelledBill(bill);
        bill.setPaymentStatus(PaymentStatus.FAILED);

        return billMapper.toDto(billRepository.save(bill));
    }

    private void restoreInventoryForCancelledBill(Bill bill) {
        List<Long> serviceIds = new ArrayList<>();
        List<Long> retailProductIds = new ArrayList<>();

        bill.getDetails().forEach(detail -> {
            if (detail.getService() != null) serviceIds.add(detail.getService().getId());
            else if (detail.getProduct() != null) retailProductIds.add(detail.getProduct().getId());
        });

        Map<Long, List<ServiceRecipe>> recipesByServiceId = fetchRecipesByServiceId(serviceIds);
        Set<Long> productIdsToLock = buildProductIdsToLock(retailProductIds, recipesByServiceId);

        if (productIdsToLock.isEmpty()) return;

        Map<Long, Product> lockedProducts = getLockedProducts(productIdsToLock);
        applyInventoryRestoration(bill, recipesByServiceId, lockedProducts);
    }

    private Set<Long> buildProductIdsToLock(List<Long> retailProductIds, Map<Long, List<ServiceRecipe>> recipes) {
        Set<Long> ids = new HashSet<>(retailProductIds);
        recipes.values().stream()
                .flatMap(List::stream)
                .map(recipe -> recipe.getProduct().getId())
                .forEach(ids::add);
        return ids;
    }

    private Map<Long, Product> getLockedProducts(Set<Long> productIds) {
        return productRepository.findByIdsWithLock(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private void applyInventoryRestoration(Bill bill, Map<Long, List<ServiceRecipe>> recipes, Map<Long, Product> lockedProducts) {
        for (BillDetail detail : bill.getDetails()) {
            BigDecimal qty = BigDecimal.valueOf(detail.getQuantity());

            if (detail.getProduct() != null) {
                restoreRetailProduct(detail.getProduct().getId(), qty, lockedProducts);
            } else if (detail.getService() != null) {
                restoreServiceIngredients(detail.getService().getId(), qty, recipes, lockedProducts);
            }
        }
    }

    private void restoreRetailProduct(Long productId, BigDecimal quantityToRestore, Map<Long, Product> lockedProducts) {
        Product p = lockedProducts.get(productId);
        if (p == null) {
            throw new ResourceNotFoundException("Product not found with ID: " + productId + ". Cannot restore inventory");
        }
        BigDecimal factor = p.getConversionFactor() != null ? p.getConversionFactor() : BigDecimal.ONE;
        p.setQuantityOnHand(p.getQuantityOnHand().add(quantityToRestore.multiply(factor)));
    }

    private void restoreServiceIngredients(Long serviceId, BigDecimal serviceQuantity, Map<Long, List<ServiceRecipe>> recipes, Map<Long, Product> lockedProducts) {
        List<ServiceRecipe> serviceRecipes = recipes.getOrDefault(serviceId, List.of());
        for (ServiceRecipe r : serviceRecipes) {
            Product p = lockedProducts.get(r.getProduct().getId());
            if (p == null) {
                throw new ResourceNotFoundException("Product not found with ID: " + r.getProduct().getId() + ". Cannot restore inventory");
            }
            p.setQuantityOnHand(p.getQuantityOnHand().add(r.getQuantityConsumed().multiply(serviceQuantity)));
        }
    }
}
